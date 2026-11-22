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
import java.util.TreeMap;

import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusEvaluation2D;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.LocusInstrumentation2D;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.conditioned.LocusConditionedCell2D;
import org.geocedg.common.kernel.locus.conditioned.LocusConditionedEvidence2D;
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

import jdk.jfr.Configuration;
import jdk.jfr.Recording;
import jdk.jfr.consumer.RecordedEvent;
import jdk.jfr.consumer.RecordedFrame;
import jdk.jfr.consumer.RecordedStackTrace;
import jdk.jfr.consumer.RecordingFile;
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
 * <li>{@code profile}: a real change of a non-driver parameter ({@code excCyl}) repeated on
 * the original and on the transported document, with the work counters of every locus and an
 * in-process Flight Recorder sample of the event thread attributed, inclusively, to marker
 * frames of the kernel. Marks overlap and do not add up to the total.</li>
 * <li>{@code conditioned}: increment I0 of the conditioned use (ADR 0042). On the original
 * and on the transported document it reads the conditioned evidence that the producer of
 * each locus publishes for its revision, with the real protocol (testimony of the slice,
 * policy {@code conditioned-traversal/v1}): cells by kind, regions, evaluations and time of
 * the first request, time of a repeated request, and, after each real change of {@code
 * excCyl}, the time of the update of the graph, of the evidence of the new revision and of
 * an off-screen paint.</li>
 * <li>{@code total}: the whole cost of a real change of {@code excCyl} with the conditioned
 * evidence consumed (decision D-V6; corrections after block 3B, step C5). On each document,
 * after a warm-up, three series: regular changes, changes across the bite of the cone and
 * loss with recovery. Each change is measured in three separate parts, the update of the
 * graph, the conditioned evidence of every locus of the new revision and an off-screen
 * paint, and the report gives the median, the 95th percentile and the maximum of each part
 * and of their sum, with a digest of what every locus publishes at every step. The mode of
 * the memo of nested evaluations is read from the process and printed, so that a run
 * without the memo is the oracle of a run with it: equal digests, different times.</li>
 * <li>{@code intersect}: the cost that one intersection on a conditioned locus adds to a real
 * change of {@code excCyl} (final corrective intervention of 1.2.2). On the original, the
 * same series of real changes is measured without the consumer, with {@code Intersect(w_2,
 * segment)} and with a point on its root, each change by part, with the median and the 95th
 * percentile. The series with the intersection is sampled by Flight Recorder and attributed
 * as a call tree under the producer of the intersection, and the work counters of the locus
 * and of the intersection are read per change. A digest of every published value and of
 * every published root, with its coordinates and its classification, makes a run the oracle
 * of another; the token of a root names the result object of the run and is counted within
 * the series.</li>
 * </ul>
 *
 * Latencies are telemetry of one machine and one short series. They are never a threshold.
 */
@ExtendWith({G9U1TestApp.Lifecycle.class,
		PreG9BR6PlusD1DocumentUnitsTest.EmptyUnitPreferences.class,
		PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class})
@EnabledIfSystemProperty(named = "geocedg.postr1.harness",
		matches = "variant|benchmark|evidence|profile|conditioned|total|intersect")
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
						: "conditioned".equals(scenario) ? conditioned(work)
						: "total".equals(scenario) ? total(work)
						: "intersect".equals(scenario) ? intersect(work, output)
						: "profile".equals(scenario) ? profile(work, output) : benchmark(work));
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

	/** Marker frames of the inclusive attribution, as substrings of {@code Class.method}. */
	private static final List<String> MARKERS = List.of(
			"AlgoDependentPointLocusV2.compute",
			"AlgoDependentPointLocusV2.synchronizeEvaluatorWithCurrentSlice",
			"ReconstructionSliceFingerprint2D.snapshot",
			"ReconstructibleLocusEvaluator2D.<init>",
			"MacroKernel.loadXML", "Macro.buildMacroXML",
			"ReconstructibleLocusEvaluator2D.validateFidelity",
			"ReconstructibleLocusEvaluator2D.captureV3",
			"ReconstructibleLocusEvaluator2D.settle",
			"AlgoDependentPointLocusV2.getHybridExistenceCertificate",
			"HybridExistenceCertifier2D", "LocusExploratoryEvidence2D",
			"ReconstructibleLocusEvaluator2D.evaluate",
			"ReconstructibleLocusEvaluator2D.applyCoordinate",
			"AlgoLocusIntersectionV2.compute", "LocusIntersectionSolver2D.intersect",
			"PublicIntersectionRootIdentityResolver2D", "AlgoLocusIntersectionPointV2",
			"AlgoLocusBetweenMetricV2", "AlgoLocusMetric", "LocusMetric",
			"AlgoSemanticLocusPoint2D.compute", "AlgoMacro",
			"SpatialIdentityRegistry", "GeoElement.getXML", "AlgoElement.getXML",
			"DrawLocusV2", "EuclidianView");

	private static List<String> profile(Path work, Path output) throws Exception {
		List<String> report = new ArrayList<>();
		report.add("# post-R1 profile of a real parameter change (excCyl). Times in ms.");
		report.add("jvm=" + System.getProperty("java.vm.name") + " "
				+ System.getProperty("java.version") + " processors="
				+ Runtime.getRuntime().availableProcessors());
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
			GeoNumeric eccentricity = (GeoNumeric) app.getKernel().lookupLabel("excCyl");
			double value = eccentricity.getValue();
			for (int index = 0; index < 4; index++) {
				PreG9BCu0Corpus.setNumber(app, "excCyl", value + (index % 2 == 0 ? 0.05 : 0));
				paint(view);
			}
			Map<String, long[]> before = allCounters(app);
			List<Double> dag = new ArrayList<>();
			List<Double> paint = new ArrayList<>();
			Path recordingFile = output.resolve("profile-" + document + ".jfr");
			try (Recording recording = new Recording(Configuration.getConfiguration(
					"profile"))) {
				recording.start();
				for (int index = 0; index < 20; index++) {
					long start = System.nanoTime();
					PreG9BCu0Corpus.setNumber(app, "excCyl",
							value + (index % 2 == 0 ? 0.05 : 0));
					long moved = System.nanoTime();
					paint(view);
					dag.add((moved - start) / 1e6);
					paint.add((System.nanoTime() - moved) / 1e6);
				}
				recording.stop();
				recording.dump(recordingFile);
			}
			report.add(row(document, "excCyl +0.05 / back, regular", 4, dag, paint, before,
					allCounters(app)));
			report.add(document + " | p95 dag=" + String.format(Locale.ROOT, "%.1f",
					percentile(dag, 0.95)));
			attribute(report, document, recordingFile);
			Files.deleteIfExists(recordingFile);
		}
		return report;
	}

	private static double percentile(List<Double> values, double fraction) {
		double[] sorted = values.stream().mapToDouble(Double::doubleValue).sorted().toArray();
		return sorted[Math.min(sorted.length - 1, (int) Math.ceil(fraction * sorted.length)
				- 1)];
	}

	private static void attribute(List<String> report, String document, Path recordingFile)
			throws Exception {
		Map<String, Integer> inclusive = new LinkedHashMap<>();
		Map<String, Integer> top = new TreeMap<>();
		int samples = 0;
		for (RecordedEvent event : RecordingFile.readAllEvents(recordingFile)) {
			if (!"jdk.ExecutionSample".equals(event.getEventType().getName())) {
				continue;
			}
			RecordedStackTrace trace = event.getStackTrace();
			if (trace == null || event.getThread("sampledThread") == null || !String.valueOf(
					event.getThread("sampledThread").getJavaName()).startsWith("AWT-EventQueue")) {
				continue;
			}
			samples++;
			List<String> frames = new ArrayList<>();
			for (RecordedFrame frame : trace.getFrames()) {
				String type = frame.getMethod().getType().getName();
				frames.add(type.substring(type.lastIndexOf('.') + 1) + "."
						+ frame.getMethod().getName());
			}
			for (String marker : MARKERS) {
				for (String frame : frames) {
					if (frame.contains(marker)) {
						inclusive.merge(marker, 1, Integer::sum);
						break;
					}
				}
			}
			if (!frames.isEmpty()) {
				top.merge(frames.get(0), 1, Integer::sum);
			}
		}
		report.add(document + " | samples on the event thread=" + samples);
		for (String marker : MARKERS) {
			int count = inclusive.getOrDefault(marker, 0);
			if (count > 0) {
				report.add(String.format(Locale.ROOT, "%s | inclusive %5.1f%% | %s", document,
						100.0 * count / Math.max(1, samples), marker));
			}
		}
		int total = samples;
		top.entrySet().stream().sorted((a, b) -> b.getValue() - a.getValue()).limit(14)
				.forEach(entry -> report.add(String.format(Locale.ROOT,
						"%s | self %5.1f%% | %s", document, 100.0 * entry.getValue()
								/ Math.max(1, total), entry.getKey())));
	}

	/** The counters of every Locus V2 and Locus V2 intersection of the document. */
	private static Map<String, long[]> allCounters(AppGeoCeDG app) {
		Map<String, long[]> counters = new LinkedHashMap<>();
		for (GeoElement geo : app.getKernel().getConstruction().getGeoSetConstructionOrder()) {
			if (geo instanceof GeoLocusV2) {
				LocusInstrumentation2D instrumentation = ((GeoLocusV2) geo).getInstrumentation();
				counters.put(geo.getLabelSimple() + "[rev/build/eval/nested]", new long[] {
						instrumentation.getRevisionPublications(),
						instrumentation.getDependencySliceBuilds(),
						instrumentation.getEvaluatorCalls(),
						instrumentation.getNestedEvaluatorCalls()});
			} else if (geo instanceof GeoLocusIntersectionResult
					&& geo.getParentAlgorithm() != null) {
				counters.put(geo.getLabelSimple() + "[resolved/equivalent]", new long[] {
						count(geo.getParentAlgorithm(), "getResolutionCount"),
						count(geo.getParentAlgorithm(), "getEquivalentUpdateCount")});
			}
		}
		return counters;
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
			String document = transported ? "transport" : "original";
			// Opening: three loads of a fresh working copy; the last one is measured on.
			List<Double> opening = new ArrayList<>();
			AppGeoCeDG app = null;
			Path variant = PreG9BCu0Corpus.repositoryRoot().resolve(PACKAGE).resolve("derived")
					.resolve(VARIANT);
			for (int index = 0; index < 3; index++) {
				long start = System.nanoTime();
				app = transported && Files.isRegularFile(variant)
						? PreG9BCu0Corpus.open(copy(variant, work)) : open(work);
				opening.add((System.nanoTime() - start) / 1e6);
			}
			if (transported && !Files.isRegularFile(variant)) {
				buildTransport(app);
			}
			report.add(document + " | open (three loads) | ms=" + String.format(Locale.ROOT,
					"%.0f / %.0f / %.0f", opening.get(0), opening.get(1), opening.get(2)));
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
			report.add(document + " | excCyl change | p95 dag=" + String.format(Locale.ROOT,
					"%.1f", percentile(times, 0.95)));
			// Changes of topology of the cone-cylinder family: above about 3.657 the cylinder
			// leaves the cone for part of the turn of N.
			series(report, app, document, "excCyl across the bite (3.60 / 3.70 / 3.80)",
					new double[] {3.60, 3.70, 3.60, 3.70, 3.80, 3.60});
			// Loss of the whole first family and recovery.
			series(report, app, document, "excCyl loss and recovery (4.2 / saved)",
					new double[] {4.2, value, 4.2, value});
			StringBuilder recovered = new StringBuilder(document + " | after recovery |");
			for (String label : LOCI) {
				GeoElement geo = app.getKernel().lookupLabel(label);
				if (geo instanceof GeoLocusV2) {
					recovered.append(' ').append(label).append('=').append(
							((GeoLocusV2) geo).getSemanticDefinition() != null ? "published"
									: "unpublished");
				}
			}
			report.add(recovered.toString());
		}
		return report;
	}

	private static List<String> conditioned(Path work) throws Exception {
		List<String> report = new ArrayList<>();
		report.add("# conditioned evidence of each locus, real protocol. Times in ms.");
		report.add("jvm=" + System.getProperty("java.vm.name") + " "
				+ System.getProperty("java.version") + " os=" + System.getProperty("os.name")
				+ " processors=" + Runtime.getRuntime().availableProcessors());
		for (boolean transported : new boolean[] {false, true}) {
			String document = transported ? "transport" : "original";
			Path variant = PreG9BCu0Corpus.repositoryRoot().resolve(PACKAGE).resolve("derived")
					.resolve(VARIANT);
			AppGeoCeDG app = transported && Files.isRegularFile(variant)
					? PreG9BCu0Corpus.open(copy(variant, work)) : open(work);
			if (transported && !Files.isRegularFile(variant)) {
				buildTransport(app);
			}
			EuclidianView view = app.getEuclidianView1();
			((EuclidianViewD) view).getJPanel().setSize(VIEW_WIDTH, VIEW_HEIGHT);
			view.updateSize();
			view.setCoordSystem(VIEW_X_ZERO, VIEW_Y_ZERO, VIEW_SCALE, VIEW_SCALE);
			paint(view);
			for (int pass = 0; pass < 2; pass++) {
				for (String label : LOCI) {
					report.add(document + " | " + (pass == 0 ? "first" : "repeated")
							+ " request | " + conditionedRow(app, label));
				}
			}
			GeoNumeric eccentricity = (GeoNumeric) app.getKernel().lookupLabel("excCyl");
			double value = eccentricity.getValue();
			for (int index = 0; index < 6; index++) {
				long start = System.nanoTime();
				PreG9BCu0Corpus.setNumber(app, "excCyl", value + (index % 2 == 0 ? 0.05 : 0));
				double dag = (System.nanoTime() - start) / 1e6;
				StringBuilder row = new StringBuilder(document + " | excCyl change " + index
						+ " | dag=" + String.format(Locale.ROOT, "%.0f", dag));
				double evidence = 0;
				for (String label : LOCI) {
					long requested = System.nanoTime();
					String cells = conditionedRow(app, label);
					double millis = (System.nanoTime() - requested) / 1e6;
					evidence += millis;
					if (!cells.endsWith("none")) {
						row.append(" | ").append(cells);
					}
				}
				long painting = System.nanoTime();
				paint(view);
				row.append(" | evidence total=").append(String.format(Locale.ROOT, "%.0f",
						evidence)).append(" paint=").append(String.format(Locale.ROOT, "%.0f",
								(System.nanoTime() - painting) / 1e6));
				report.add(row.toString());
			}
		}
		return report;
	}

	private static List<String> total(Path work) throws Exception {
		List<String> report = new ArrayList<>();
		report.add("# whole cost of a real change of excCyl, by part. Times in ms: graph ="
				+ " update of the dependency graph; evidence = conditioned evidence of every"
				+ " locus of the new revision; paint = off-screen image of Graphics 1.");
		report.add("jvm=" + System.getProperty("java.vm.name") + " "
				+ System.getProperty("java.version") + " os=" + System.getProperty("os.name")
				+ " processors=" + Runtime.getRuntime().availableProcessors()
				+ " nested-evaluation-memo=" + org.geocedg.common.kernel.locus
						.LocusNestedEvaluationMemo2D.mode());
		for (boolean transported : new boolean[] {false, true}) {
			String document = transported ? "transport" : "original";
			Path variant = PreG9BCu0Corpus.repositoryRoot().resolve(PACKAGE).resolve("derived")
					.resolve(VARIANT);
			AppGeoCeDG app = transported && Files.isRegularFile(variant)
					? PreG9BCu0Corpus.open(copy(variant, work)) : open(work);
			if (transported && !Files.isRegularFile(variant)) {
				buildTransport(app);
			}
			EuclidianView view = app.getEuclidianView1();
			((EuclidianViewD) view).getJPanel().setSize(VIEW_WIDTH, VIEW_HEIGHT);
			view.updateSize();
			view.setCoordSystem(VIEW_X_ZERO, VIEW_Y_ZERO, VIEW_SCALE, VIEW_SCALE);
			paint(view);
			double value = ((GeoNumeric) app.getKernel().lookupLabel("excCyl")).getValue();
			double[] warmUp = new double[4];
			double[] regular = new double[20];
			for (int index = 0; index < regular.length; index++) {
				regular[index] = value + (index % 2 == 0 ? 0.05 : 0);
			}
			for (int index = 0; index < warmUp.length; index++) {
				warmUp[index] = value + (index % 2 == 0 ? 0.05 : 0);
			}
			totalSeries(report, app, view, document, "warm-up (not a measure)", warmUp);
			totalSeries(report, app, view, document, "regular, +0.05 and back", regular);
			// Changes of topology of the cone-cylinder family: above about 3.657 the cylinder
			// leaves the cone for part of the turn of N.
			totalSeries(report, app, view, document, "across the bite (3.60 / 3.70 / 3.80)",
					new double[] {3.60, 3.70, 3.60, 3.70, 3.80, 3.60, 3.70, 3.80, value});
			// Loss of the whole first family, a degenerate configuration, and recovery.
			totalSeries(report, app, view, document, "loss and recovery (4.2 / saved)",
					new double[] {4.2, value, 4.2, value, 4.2, value});
		}
		return report;
	}

	private static List<String> intersect(Path work, Path output) throws Exception {
		List<String> report = new ArrayList<>();
		report.add("# cost that each consumer of the conditioned locus w_2 adds to a real change"
				+ " of excCyl. Every configuration is a fresh copy of the original with the"
				+ " consumers named. Times in ms: graph = update of the dependency graph, which"
				+ " contains the consumers; evidence = conditioned evidence of every locus;"
				+ " paint = off-screen image of Graphics 1.");
		report.add("jvm=" + System.getProperty("java.vm.name") + " "
				+ System.getProperty("java.version") + " os=" + System.getProperty("os.name")
				+ " processors=" + Runtime.getRuntime().availableProcessors()
				+ " nested-evaluation-memo=" + System.getProperty(
						"geocedg.nestedEvaluationMemo", "on"));
		String only = System.getProperty("geocedg.postr1.configuration", "");
		// length: LocusLength between two points of one region; total: LocusLength of the
		// whole locus; cross: Intersect with a segment across the first region; point: a
		// point on the root of that intersection.
		for (String configuration : new String[] {"none", "length", "total", "cross",
				"cross+point", "length+total+cross+point"}) {
			if (!only.isEmpty() && !only.equals(configuration)) {
				continue;
			}
			intersectConfiguration(report, work, output, configuration);
		}
		return report;
	}

	private static void intersectConfiguration(List<String> report, Path work, Path output,
			String configuration) throws Exception {
		AppGeoCeDG app = open(work);
		EuclidianView view = app.getEuclidianView1();
		((EuclidianViewD) view).getJPanel().setSize(VIEW_WIDTH, VIEW_HEIGHT);
		view.updateSize();
		view.setCoordSystem(VIEW_X_ZERO, VIEW_Y_ZERO, VIEW_SCALE, VIEW_SCALE);
		paint(view);
		double value = ((GeoNumeric) app.getKernel().lookupLabel("excCyl")).getValue();
		double[] warmUp = new double[4];
		double[] regular = new double[12];
		for (int index = 0; index < regular.length; index++) {
			regular[index] = value + (index % 2 == 0 ? 0.05 : 0);
		}
		for (int index = 0; index < warmUp.length; index++) {
			warmUp[index] = value + (index % 2 == 0 ? 0.05 : 0);
		}
		GeoLocusV2 hole = PreG9BCu0Corpus.locus(app, "w_2");
		LocusConditionedEvidence2D evidence = hole.getConditionedEvidence("generator.main");
		LocusInterval2D region = evidence.getRegions().get(0);
		double span = region.getUpper() - region.getLower();
		List<String> parts = Arrays.asList(configuration.split("\\+"));
		String crossing = null;
		if (parts.contains("length")) {
			command(app, "CUP=Point(w_2,\"generator.main\"," + (region.getLower() + 0.3 * span)
					+ ")");
			command(app, "CUQ=Point(w_2,\"generator.main\"," + (region.getLower() + 0.7 * span)
					+ ")");
			command(app, "cuL=LocusLength(w_2,CUP,CUQ)");
			command(app, "cuF=2*cuL+1");
		}
		if (parts.contains("total")) {
			command(app, "cuT=LocusLength(w_2)");
		}
		if (parts.contains("cross")) {
			// A segment across w_2 in the middle of its first conditioned region.
			double at = region.getLower() + 0.5 * span;
			double[] here = holePoint(hole, at);
			double[] before = holePoint(hole, at - 1e-4);
			double[] after = holePoint(hole, at + 1e-4);
			double norm = Math.hypot(after[0] - before[0], after[1] - before[1]);
			double nx = -(after[1] - before[1]) / norm * 0.05;
			double ny = (after[0] - before[0]) / norm * 0.05;
			command(app, "CUA=(" + (here[0] - nx) + "," + (here[1] - ny) + ")");
			command(app, "CUB=(" + (here[0] + nx) + "," + (here[1] + ny) + ")");
			command(app, "cuS=Segment(CUA,CUB)");
			long created = System.nanoTime();
			command(app, "cuV=Intersect(w_2,cuS)");
			report.add(configuration + " | creation of Intersect(w_2, segment) ms="
					+ String.format(Locale.ROOT, "%.0f", (System.nanoTime() - created) / 1e6));
			crossing = "cuV";
		}
		if (parts.contains("point")) {
			GeoLocusIntersectionResult rich = (GeoLocusIntersectionResult) app.getKernel()
					.lookupLabel("cuV");
			command(app, "CUR=Intersect(cuV,\"" + rich.getIntersectionResult()
					.getFiniteSolutions().get(0).getIdentity().getRootToken() + "\")");
		}
		intersectSeries(report, app, view, configuration + " | warm-up (not a measure)",
				warmUp, crossing);
		Path recordingFile = output.resolve("intersect-" + configuration.replace('+', '-')
				+ ".jfr");
		try (Recording recording = new Recording(Configuration.getConfiguration("profile"))) {
			recording.start();
			intersectSeries(report, app, view, configuration + " | regular, +0.05 and back",
					regular, crossing);
			recording.stop();
			recording.dump(recordingFile);
		}
		if (!"none".equals(configuration)) {
			attribute(report, configuration, recordingFile);
			for (String root : new String[] {"AlgoLocusIntersectionV2.compute",
					"AlgoLocusMetricV2.compute", "AlgoLocusBetweenMetricV2.compute"}) {
				tree(report, configuration, recordingFile, root, 7, 3.0);
			}
		}
		Files.deleteIfExists(recordingFile);
		if (crossing != null) {
			// The same consumers where the locus changes its regions, and where it is lost.
			intersectSeries(report, app, view, configuration
					+ " | across the bite (3.60 / 3.70 / 3.80)",
					new double[] {3.60, 3.70, 3.60, 3.70, 3.80, 3.60, 3.70, 3.80, value},
					crossing);
			intersectSeries(report, app, view, configuration
					+ " | loss and recovery (4.2 / saved)",
					new double[] {4.2, value, 4.2, value, 4.2, value}, crossing);
		}
		for (String label : new String[] {"cuL", "cuF", "cuT", "CUR"}) {
			GeoElement geo = app.getKernel().lookupLabel(label);
			if (geo != null) {
				report.add(configuration + " | " + label + " defined=" + geo.isDefined()
						+ " value=" + geo.toValueString(
								org.geogebra.common.kernel.StringTemplate.maxPrecision));
			}
		}
	}

	private static double[] holePoint(GeoLocusV2 locus, double parameter) {
		LocusEvaluation2D evaluation = locus.evaluate("generator.main", parameter,
				LocusEvaluationSession2D.reference());
		return new double[] {evaluation.getPoint().getX(), evaluation.getPoint().getY()};
	}

	private static void command(AppGeoCeDG app, String text) {
		GeoElementND[] result = app.getKernel().getAlgebraProcessor().processAlgebraCommand(
				text, false);
		if (result == null || result.length == 0) {
			throw new IllegalStateException("Command produced nothing: " + text);
		}
	}

	/**
	 * One series of real changes with or without an intersection on w_2: each change by part,
	 * the work counters of w_2 and of the intersection, and a digest of the published roots.
	 */
	private static void intersectSeries(List<String> report, AppGeoCeDG app,
			EuclidianView view, String name, double[] values, String intersection)
			throws Exception {
		java.security.MessageDigest digest = java.security.MessageDigest.getInstance(
				"SHA-256");
		List<Double> graph = new ArrayList<>();
		List<Double> evidence = new ArrayList<>();
		List<Double> painting = new ArrayList<>();
		List<Double> sum = new ArrayList<>();
		LocusInstrumentation2D hole = PreG9BCu0Corpus.locus(app, "w_2").getInstrumentation();
		long calls = hole.getEvaluatorCalls();
		long nested = hole.getNestedEvaluatorCalls();
		GeoElement crossing = intersection == null ? null
				: app.getKernel().lookupLabel(intersection);
		long resolved = crossing == null ? 0
				: count(crossing.getParentAlgorithm(), "getResolutionCount");
		StringBuilder shape = new StringBuilder();
		java.util.Set<String> tokens = new java.util.TreeSet<>();
		for (double value : values) {
			long start = System.nanoTime();
			PreG9BCu0Corpus.setNumber(app, "excCyl", value);
			long updated = System.nanoTime();
			for (String label : LOCI) {
				GeoElement geo = app.getKernel().lookupLabel(label);
				if (geo instanceof GeoLocusV2
						&& ((GeoLocusV2) geo).getSemanticDefinition() != null) {
					((GeoLocusV2) geo).getConditionedEvidence("generator.main");
				}
			}
			long evidenced = System.nanoTime();
			paint(view);
			long painted = System.nanoTime();
			graph.add((updated - start) / 1e6);
			evidence.add((evidenced - updated) / 1e6);
			painting.add((painted - evidenced) / 1e6);
			sum.add((painted - start) / 1e6);
			observe(digest, app);
			for (String label : new String[] {"cuL", "cuF", "cuT", "CUR"}) {
				GeoElement consumer = app.getKernel().lookupLabel(label);
				if (consumer != null) {
					digest.update((label + "=" + consumer.isDefined() + ":"
							+ consumer.toValueString(
									org.geogebra.common.kernel.StringTemplate.maxPrecision))
							.getBytes(StandardCharsets.UTF_8));
				}
			}
			if (crossing instanceof GeoLocusIntersectionResult) {
				LocusIntersectionResult2D result = ((GeoLocusIntersectionResult) crossing)
						.getIntersectionResult();
				StringBuilder text = new StringBuilder(String.valueOf(
						result.getComputationStatus())).append('/').append(
								result.getGeometryKind()).append('/').append(
										result.getCompletenessEvidence().getCompleteness());
				for (LocusIntersectionSolution2D root : result.getFiniteSolutions()) {
					text.append(';').append(Double.toHexString(root.getEvaluatedPoint().getX()))
							.append(',').append(Double.toHexString(root.getEvaluatedPoint()
									.getY())).append(',').append(root.getIdentity()
											.getIdentityStatus())
							.append(',').append(root.getClassification().getContactClass());
					// The token names the result object of this run, whose identity is
					// allocated by the run: it is compared within the series, not across.
					tokens.add(root.getIdentity().getRootToken());
				}
				digest.update(text.toString().getBytes(StandardCharsets.UTF_8));
				shape.append(result.getFiniteSolutions().size());
			}
		}
		StringBuilder hex = new StringBuilder();
		for (byte part : digest.digest()) {
			hex.append(String.format("%02x", part));
		}
		report.add(name + " | changes=" + values.length + " | graph " + partSummary(graph)
				+ " | evidence " + partSummary(evidence) + " | paint " + partSummary(painting)
				+ " | total " + partSummary(sum) + " | w_2 evaluator calls per change="
				+ (hole.getEvaluatorCalls() - calls) / values.length + " nested="
				+ (hole.getNestedEvaluatorCalls() - nested) / values.length
				+ (crossing == null ? "" : " | resolutions=" + (count(
						crossing.getParentAlgorithm(), "getResolutionCount") - resolved)
						+ " roots per change=" + shape + " distinct tokens=" + tokens.size())
				+ " | results=" + hex.substring(0, 32));
	}

	/**
	 * Call tree of the event-thread samples below the outermost frame that contains a marker:
	 * the share of each callee path, to a depth, as a percentage of the samples under the
	 * marker.
	 */
	private static void tree(List<String> report, String document, Path recordingFile,
			String marker, int depth, double minimumShare) throws Exception {
		Map<String, Integer> paths = new TreeMap<>();
		int under = 0;
		int all = 0;
		for (RecordedEvent event : RecordingFile.readAllEvents(recordingFile)) {
			if (!"jdk.ExecutionSample".equals(event.getEventType().getName())) {
				continue;
			}
			RecordedStackTrace trace = event.getStackTrace();
			if (trace == null || event.getThread("sampledThread") == null || !String.valueOf(
					event.getThread("sampledThread").getJavaName()).startsWith("AWT-EventQueue")) {
				continue;
			}
			all++;
			List<String> frames = new ArrayList<>();
			for (RecordedFrame frame : trace.getFrames()) {
				String type = frame.getMethod().getType().getName();
				frames.add(type.substring(type.lastIndexOf('.') + 1) + "."
						+ frame.getMethod().getName());
			}
			int root = -1;
			for (int index = frames.size() - 1; index >= 0; index--) {
				if (frames.get(index).contains(marker)) {
					root = index;
					break;
				}
			}
			if (root < 0) {
				continue;
			}
			under++;
			StringBuilder path = new StringBuilder();
			String last = frames.get(root);
			int level = 0;
			for (int index = root - 1; index >= 0 && level < depth; index--) {
				// Recursion and lambdas of one method are one level.
				String frame = frames.get(index);
				String owner = frame.replaceAll("\\.lambda\\$([A-Za-z0-9_]+)\\$\\d+", ".$1");
				if (owner.equals(last)) {
					continue;
				}
				last = owner;
				path.append(level == 0 ? "" : " > ").append(owner);
				paths.merge(path.toString(), 1, Integer::sum);
				level++;
			}
		}
		if (under == 0) {
			return;
		}
		report.add(String.format(Locale.ROOT,
				"%s | tree | %s | samples under it=%d of %d (%.1f%%)", document, marker, under,
				all, 100.0 * under / Math.max(1, all)));
		int total = under;
		for (Map.Entry<String, Integer> entry : paths.entrySet()) {
			double share = 100.0 * entry.getValue() / Math.max(1, total);
			if (share >= minimumShare) {
				report.add(String.format(Locale.ROOT, "%s | tree | %5.1f%% | %s", document,
						share, entry.getKey()));
			}
		}
	}

	/** One series of real changes, each measured by part, with a digest of its results. */
	private static void totalSeries(List<String> report, AppGeoCeDG app, EuclidianView view,
			String document, String name, double[] values) throws Exception {
		java.security.MessageDigest digest = java.security.MessageDigest.getInstance(
				"SHA-256");
		List<Double> graph = new ArrayList<>();
		List<Double> evidence = new ArrayList<>();
		List<Double> painting = new ArrayList<>();
		List<Double> sum = new ArrayList<>();
		for (double value : values) {
			long start = System.nanoTime();
			PreG9BCu0Corpus.setNumber(app, "excCyl", value);
			long updated = System.nanoTime();
			for (String label : LOCI) {
				GeoElement geo = app.getKernel().lookupLabel(label);
				if (geo instanceof GeoLocusV2
						&& ((GeoLocusV2) geo).getSemanticDefinition() != null) {
					((GeoLocusV2) geo).getConditionedEvidence("generator.main");
				}
			}
			long evidenced = System.nanoTime();
			paint(view);
			long painted = System.nanoTime();
			graph.add((updated - start) / 1e6);
			evidence.add((evidenced - updated) / 1e6);
			painting.add((painted - evidenced) / 1e6);
			sum.add((painted - start) / 1e6);
			observe(digest, app);
		}
		StringBuilder hex = new StringBuilder();
		for (byte part : digest.digest()) {
			hex.append(String.format("%02x", part));
		}
		report.add(document + " | " + name + " | changes=" + values.length + " | graph "
				+ partSummary(graph) + " | evidence " + partSummary(evidence) + " | paint "
				+ partSummary(painting) + " | total " + partSummary(sum) + " | results="
				+ hex.substring(0, 32));
	}

	private static String partSummary(List<Double> values) {
		double[] sorted = values.stream().mapToDouble(Double::doubleValue).sorted().toArray();
		return String.format(Locale.ROOT, "median=%.0f p95=%.0f max=%.0f",
				sorted[sorted.length / 2], percentile(values, 0.95),
				sorted[sorted.length - 1]);
	}

	/** What every locus of the document publishes now, as exact text, into a digest. */
	private static void observe(java.security.MessageDigest digest, AppGeoCeDG app) {
		StringBuilder text = new StringBuilder();
		for (String label : LOCI) {
			GeoElement geo = app.getKernel().lookupLabel(label);
			if (!(geo instanceof GeoLocusV2)) {
				continue;
			}
			GeoLocusV2 locus = (GeoLocusV2) geo;
			text.append(label).append('=');
			if (locus.getSemanticDefinition() == null) {
				text.append("unpublished;");
				continue;
			}
			text.append(locus.getSemanticDefinition().getBranch("generator.main")
					.getExistenceStructure().getSemanticSignature()).append(';');
			LocusConditionedEvidence2D evidence = locus.getConditionedEvidence(
					"generator.main");
			if (evidence != null) {
				for (LocusConditionedCell2D cell : evidence.getCells()) {
					text.append(cell.getKind()).append(':').append(cell.getSpanClass())
							.append(':').append(Double.toHexString(cell.getLower())).append(':')
							.append(Double.toHexString(cell.getUpper())).append(';');
				}
			}
			for (int index = 0; index < 16; index++) {
				LocusEvaluation2D evaluation = locus.evaluate("generator.main",
						-3.1 + index * 0.4, LocusEvaluationSession2D.reference());
				text.append(evaluation.isValid() ? Double.toHexString(evaluation.getPoint()
						.getX()) + "," + Double.toHexString(evaluation.getPoint().getY())
						: String.valueOf(evaluation.getStatus())).append(';');
			}
		}
		for (String label : new String[] {"N", "E_1"}) {
			GeoElement geo = app.getKernel().lookupLabel(label);
			text.append(label).append('=').append(geo == null ? "none"
					: geo.toValueString(org.geogebra.common.kernel.StringTemplate.maxPrecision))
					.append(';');
		}
		digest.update(text.toString().getBytes(StandardCharsets.UTF_8));
	}

	/** One locus: revision, time of the request and content of its conditioned evidence. */
	private static String conditionedRow(AppGeoCeDG app, String label) {
		GeoElement geo = app.getKernel().lookupLabel(label);
		if (!(geo instanceof GeoLocusV2) || ((GeoLocusV2) geo).getSemanticDefinition() == null) {
			return label + ": none";
		}
		GeoLocusV2 locus = (GeoLocusV2) geo;
		long start = System.nanoTime();
		LocusConditionedEvidence2D evidence = locus.getConditionedEvidence("generator.main");
		double millis = (System.nanoTime() - start) / 1e6;
		if (evidence == null) {
			return label + ": none";
		}
		Map<String, Integer> kinds = new TreeMap<>();
		Map<String, Integer> classes = new TreeMap<>();
		Map<String, Integer> causes = new TreeMap<>();
		for (LocusConditionedCell2D cell : evidence.getCells()) {
			kinds.merge(cell.getKind().name(), 1, Integer::sum);
			if (cell.getSpanClass() != null) {
				classes.merge(cell.getSpanClass().name(), 1, Integer::sum);
			}
			if (!cell.getKind().isSpan() && !cell.getCause().isEmpty()) {
				String cause = cell.getCause();
				causes.merge(cell.getKind().name() + ":" + cause.substring(0,
						Math.min(70, cause.length())), 1, Integer::sum);
			}
		}
		StringBuilder regions = new StringBuilder();
		for (LocusInterval2D region : evidence.getRegions()) {
			regions.append(String.format(Locale.ROOT, "[%.4f, %.4f] ", region.getLower(),
					region.getUpper()));
		}
		return label + ": revision=" + evidence.getRevision() + " ms=" + String.format(
				Locale.ROOT, "%.0f", millis) + " work=" + evidence.getWork() + " cells="
				+ evidence.getCells().size() + " " + kinds + " classes=" + classes
				+ " regions=" + regions.toString().trim() + " causes=" + causes;
	}

	private static void series(List<String> report, AppGeoCeDG app, String document,
			String name, double[] values) {
		Map<String, long[]> before = counters(app);
		List<Double> times = new ArrayList<>();
		for (double value : values) {
			long start = System.nanoTime();
			PreG9BCu0Corpus.setNumber(app, "excCyl", value);
			times.add((System.nanoTime() - start) / 1e6);
		}
		report.add(row(document, name, 0, times, null, before, counters(app)));
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
