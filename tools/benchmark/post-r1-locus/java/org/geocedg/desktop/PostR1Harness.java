/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusEvaluation2D;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.LocusInstrumentation2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionResult2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionSolution2D;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.euclidian.EuclidianView;
import org.geogebra.common.kernel.algos.AlgoElement;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.kernelND.GeoElementND;
import org.geogebra.desktop.awt.GGraphics2DD;
import org.geogebra.desktop.euclidian.EuclidianViewD;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Post-R1 reproducible harness (tools/benchmark/post-r1-locus/run-harness.ps1). It is not a
 * verification profile and assigns no verdict.
 *
 * <ul>
 * <li>{@code variant}: loads a working copy of the author model, builds the transported hole
 * contour from the versioned command list, saves the native document and reads it back.</li>
 * <li>{@code benchmark}: on the original and on the transported document, drags of each
 * driver, a real change of a parameter, and passages through the incidence domain, with the
 * update of the dependency graph timed apart from an off-screen paint of Graphics 1 at the
 * size, origin and scale saved in the model. The embedded host has no window: the paint is
 * the product drawing code on an image, without the compositor of a real window. Counters
 * are read from the public instrumentation; the counters of the equivalent update of an
 * intersection are read by reflection so that the same harness runs on an earlier commit.</li>
 * <li>{@code evidence}: design inspection for the conditioned-use proposal. For each hole
 * locus of the original it evaluates the faithful slice at uniform parameters and reads, by
 * reflection and without changing anything, the root evidence that the isolated copy already
 * holds after each evaluation: the public evaluation result does not carry it.</li>
 * </ul>
 *
 * Latencies are telemetry of one machine and one short series. They are never a threshold.
 */
@ExtendWith({G9U1TestApp.Lifecycle.class,
		PreG9BR6PlusD1DocumentUnitsTest.EmptyUnitPreferences.class,
		PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class})
@EnabledIfSystemProperty(named = "geocedg.postr1.harness",
		matches = "variant|benchmark|evidence")
class PostR1Harness {
	private static final String ORIGINAL = "InterCilConoObliqueFinal_2.cedg";
	private static final String PACKAGE = "models/regression/post-r1-locus-on-intersection";
	private static final String VARIANT = "InterCilConoObliqueFinal_2.transport.cedg";
	private static final List<String> LOCI = List.of("o", "o_1", "e_2", "s_2", "w_2", "a_3",
			"trHole");
	private static final List<String> INTERSECTIONS = List.of("u_1", "t_2", "v_2");
	/** Graphics 1 as saved in the author model. */
	private static final int VIEW_WIDTH = 1092;
	private static final int VIEW_HEIGHT = 797;
	private static final double VIEW_X_ZERO = 190.04375359472687;
	private static final double VIEW_Y_ZERO = 421.1450265280542;
	private static final double VIEW_SCALE = 23.987955437910177;

	@Test
	void run() throws Exception {
		Path output = Path.of(System.getProperty("geocedg.postr1.output"));
		Files.createDirectories(output);
		Path work = Files.createDirectories(output.resolve("work"));
		String scenario = System.getProperty("geocedg.postr1.harness");
		List<String> report = PreG9BR6PlusF3Fixtures.onEdt(() -> "variant".equals(scenario)
				? variant(work, output) : "evidence".equals(scenario) ? evidence(work)
						: benchmark(work));
		Files.write(output.resolve(scenario + ".txt"), report, StandardCharsets.UTF_8);
		assertTrue(!report.isEmpty());
	}

	private static List<String> variant(Path work, Path output) throws Exception {
		List<String> report = new ArrayList<>();
		AppGeoCeDG app = open(work);
		buildTransport(app);
		File saved = output.resolve(VARIANT).toFile();
		report.add("saved=" + app.saveGeoGebraFile(saved));
		AppGeoCeDG reopened = PreG9BCu0Corpus.open(copy(saved.toPath(), work));
		GeoLocusV2 hole = PreG9BCu0Corpus.locus(reopened, "trHole");
		report.add("reopened trHole defined=" + hole.isDefined() + " revision="
				+ hole.getSemanticRevision());
		report.add("bytes=" + saved.length() + " sha256="
				+ PreG9BCu0Corpus.sha256(Files.readAllBytes(saved.toPath())));
		report.add("objects=" + reopened.getKernel().getConstruction()
				.getGeoSetConstructionOrder().size());
		return report;
	}

	private static List<String> evidence(Path work) throws Exception {
		List<String> report = new ArrayList<>();
		report.add("# root evidence held by the isolated slice copy after each evaluation");
		report.add("locus\tparameter\tstatus\tms\trich results\troots: status@parameter"
				+ "[isolating width; isolation; guarantee]\tseparation of the roots");
		AppGeoCeDG app = open(work);
		for (String label : new String[] {"w_2", "a_3"}) {
			GeoLocusV2 locus = PreG9BCu0Corpus.locus(app, label);
			Object evaluator = locus.getSemanticDefinition().getEvaluatorCapability();
			Field field = evaluator.getClass().getDeclaredField("isolatedConstruction");
			field.setAccessible(true);
			Construction copy = (Construction) field.get(evaluator);
			for (int index = 0; index < 64; index++) {
				double parameter = -Math.PI + 2 * Math.PI * index / 64;
				long start = System.nanoTime();
				LocusEvaluation2D evaluation = locus.evaluate("generator.main", parameter,
						LocusEvaluationSession2D.reference());
				double millis = (System.nanoTime() - start) / 1e6;
				StringBuilder roots = new StringBuilder();
				int results = 0;
				double separation = Double.NaN;
				for (GeoElement geo : copy.getGeoSetConstructionOrder()) {
					if (!(geo instanceof GeoLocusIntersectionResult)
							|| !geo.isChildOf(findDriverCopy(copy))) {
						continue;
					}
					results++;
					LocusIntersectionResult2D result = ((GeoLocusIntersectionResult) geo)
							.getIntersectionResult();
					if (result == null) {
						roots.append("unpublished ");
						continue;
					}
					List<LocusIntersectionSolution2D> solutions = result.getFiniteSolutions();
					for (LocusIntersectionSolution2D solution : solutions) {
						roots.append(solution.getIdentity().getIdentityStatus()).append('@')
								.append(String.format(Locale.ROOT, "%.6f[%.1e; %s; %s] ",
										solution.getRevisionEvidence().getSemanticParameter(),
										solution.getRevisionEvidence().getIsolatingInterval()
												.getUpper() - solution.getRevisionEvidence()
												.getIsolatingInterval().getLower(),
										solution.getRevisionEvidence().getLocalIsolationStatus(),
										solution.getRevisionEvidence().getNumericGuarantee()));
					}
					if (solutions.size() == 2) {
						separation = Math.abs(solutions.get(0).getRevisionEvidence()
								.getSemanticParameter() - solutions.get(1).getRevisionEvidence()
								.getSemanticParameter());
					}
				}
				report.add(String.format(Locale.ROOT, "%s\t%.5f\t%s\t%.2f\t%d\t%s\t%.6f", label,
						parameter, evaluation.getStatus(), millis, results, roots.toString().trim(),
						separation));
			}
		}
		return report;
	}

	/** @return the copy of the driver of the hole loci, by its label in this model */
	private static GeoElement findDriverCopy(Construction copy) {
		return copy.lookupLabel("E_1");
	}

	private static List<String> benchmark(Path work) throws Exception {
		List<String> report = new ArrayList<>();
		report.add("# post-R1 benchmark. Times in ms; dag = update of the dependency graph for"
				+ " one driver step; paint = off-screen image of Graphics 1 after it.");
		report.add("jvm=" + System.getProperty("java.vm.name") + " "
				+ System.getProperty("java.version") + " os=" + System.getProperty("os.name")
				+ " processors=" + Runtime.getRuntime().availableProcessors());
		for (boolean transported : new boolean[] {false, true}) {
			AppGeoCeDG app = open(work);
			if (transported) {
				buildTransport(app);
			}
			String document = transported ? "transport" : "original";
			EuclidianView view = app.getEuclidianView1();
			((EuclidianViewD) view).getJPanel().setSize(VIEW_WIDTH, VIEW_HEIGHT);
			view.updateSize();
			view.setCoordSystem(VIEW_X_ZERO, VIEW_Y_ZERO, VIEW_SCALE, VIEW_SCALE);
			paint(view);
			double savedN = PreG9BCu0Corpus.pathParameter(app, "N");
			// Regular interior of the incidence domain of E_1 (R1 trajectory).
			drag(report, app, document, "E_1 interior", "E_1",
					new double[] {2.17, 2.18, 2.19}, steps(2.20, 2.40, 21));
			drag(report, app, document, "N", "N", new double[] {savedN + 0.01, savedN + 0.02,
					savedN + 0.03}, steps(savedN + 0.05, savedN + 1.05, 21));
			// Out of the incidence domain and back, and across the fold.
			drag(report, app, document, "E_1 leave and return", "E_1", new double[] {2.40},
					new double[] {0.5, 2.40, 0.5, 2.40, 0.6, 2.30});
			drag(report, app, document, "E_1 near the fold", "E_1", new double[] {2.70},
					steps(2.72, 2.80, 9));
			PreG9BCu0Corpus.movePathPoint(app, "E_1", 2.30);
			// A real change of the geometry.
			GeoNumeric eccentricity = (GeoNumeric) app.getKernel().lookupLabel("excCyl");
			double value = eccentricity.getValue();
			Map<String, long[]> before = counters(app);
			List<Double> times = new ArrayList<>();
			for (int index = 0; index < 6; index++) {
				long start = System.nanoTime();
				PreG9BCu0Corpus.setNumber(app, "excCyl", value + (index % 2 == 0 ? 0.05 : 0));
				times.add((System.nanoTime() - start) / 1e6);
			}
			report.add(row(document, "excCyl change", 0, times, null, before, counters(app)));
		}
		return report;
	}

	private static void drag(List<String> report, AppGeoCeDG app, String document, String name,
			String driver, double[] warmUp, double[] positions) {
		for (double position : warmUp) {
			PreG9BCu0Corpus.movePathPoint(app, driver, position);
		}
		Map<String, long[]> before = counters(app);
		List<Double> dag = new ArrayList<>();
		List<Double> paint = new ArrayList<>();
		for (double position : positions) {
			long start = System.nanoTime();
			PreG9BCu0Corpus.movePathPoint(app, driver, position);
			long moved = System.nanoTime();
			paint(app.getEuclidianView1());
			dag.add((moved - start) / 1e6);
			paint.add((System.nanoTime() - moved) / 1e6);
		}
		report.add(row(document, name, warmUp.length, dag, paint, before, counters(app)));
	}

	private static void paint(EuclidianView view) {
		BufferedImage image = new BufferedImage(VIEW_WIDTH, VIEW_HEIGHT,
				BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics = image.createGraphics();
		graphics.setColor(Color.WHITE);
		graphics.fillRect(0, 0, VIEW_WIDTH, VIEW_HEIGHT);
		view.paint(new GGraphics2DD(graphics));
		graphics.dispose();
	}

	private static String row(String document, String name, int warmUp, List<Double> dag,
			List<Double> paint, Map<String, long[]> before, Map<String, long[]> after) {
		StringBuilder text = new StringBuilder();
		text.append(document).append(" | ").append(name).append(" | warm-up=").append(warmUp)
				.append(" steps=").append(dag.size()).append(" | dag ").append(summary(dag));
		if (paint != null) {
			text.append(" | paint ").append(summary(paint));
		}
		for (Map.Entry<String, long[]> entry : after.entrySet()) {
			long[] start = before.get(entry.getKey());
			long[] end = entry.getValue();
			text.append(" | ").append(entry.getKey()).append(':');
			for (int index = 0; index < end.length; index++) {
				text.append(index == 0 ? "" : "/").append(end[index] - start[index]);
			}
		}
		return text.toString();
	}

	private static String summary(List<Double> values) {
		double[] sorted = values.stream().mapToDouble(Double::doubleValue).sorted().toArray();
		return String.format(Locale.ROOT, "median=%.1f min=%.1f max=%.1f", sorted[sorted.length
				/ 2], sorted[0], sorted[sorted.length - 1]);
	}

	/**
	 * Per locus: revisions published / slice builds / evaluator calls / nested evaluator
	 * calls. Per intersection: resolutions / equivalent updates, or -1 when the build does not
	 * count them.
	 */
	private static Map<String, long[]> counters(AppGeoCeDG app) {
		Map<String, long[]> counters = new LinkedHashMap<>();
		for (String label : LOCI) {
			GeoElement geo = app.getKernel().lookupLabel(label);
			if (geo instanceof GeoLocusV2) {
				LocusInstrumentation2D instrumentation = ((GeoLocusV2) geo).getInstrumentation();
				counters.put(label + "[rev/build/eval/nested]", new long[] {
						instrumentation.getRevisionPublications(),
						instrumentation.getDependencySliceBuilds(),
						instrumentation.getEvaluatorCalls(),
						instrumentation.getNestedEvaluatorCalls()});
			}
		}
		for (String label : INTERSECTIONS) {
			GeoElement geo = app.getKernel().lookupLabel(label);
			if (geo != null && geo.getParentAlgorithm() != null) {
				counters.put(label + "[resolved/equivalent]", new long[] {
						count(geo.getParentAlgorithm(), "getResolutionCount"),
						count(geo.getParentAlgorithm(), "getEquivalentUpdateCount")});
			}
		}
		return counters;
	}

	private static long count(AlgoElement algorithm, String getter) {
		try {
			Method method = algorithm.getClass().getMethod(getter);
			return (Long) method.invoke(algorithm);
		} catch (ReflectiveOperationException absent) {
			return -1;
		}
	}

	private static double[] steps(double from, double to, int count) {
		double[] steps = new double[count];
		for (int index = 0; index < count; index++) {
			steps[index] = from + (to - from) * index / (count - 1);
		}
		return steps;
	}

	private static AppGeoCeDG open(Path work) throws Exception {
		Path original = PreG9BCu0Corpus.repositoryRoot().resolve(PACKAGE).resolve("original")
				.resolve(ORIGINAL);
		return PreG9BCu0Corpus.open(copy(original, work));
	}

	private static File copy(Path source, Path work) throws Exception {
		Path target = Files.createTempDirectory(work, "doc").resolve(
				source.getFileName().toString());
		Files.write(target, Files.readAllBytes(source));
		return target.toFile();
	}

	private static void buildTransport(AppGeoCeDG app) throws Exception {
		for (String line : Files.readAllLines(PreG9BCu0Corpus.repositoryRoot().resolve(PACKAGE)
				.resolve("transport-commands.txt"), StandardCharsets.UTF_8)) {
			if (!line.isBlank() && !line.startsWith("#")) {
				GeoElementND[] result = app.getKernel().getAlgebraProcessor()
						.processAlgebraCommand(line.trim(), false);
				if (result == null || result.length == 0) {
					throw new IllegalStateException("Command produced nothing: " + line + " "
							+ Arrays.toString(result));
				}
			}
		}
	}
}
