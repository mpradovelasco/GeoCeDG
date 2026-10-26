/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import org.geocedg.common.kernel.algos.AlgoLocusDriverCorrespondencePoint2D;
import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geocedg.common.kernel.locus.constructive.AuthorityPolicyAdoption;
import org.geocedg.common.kernel.locus.constructive.StructuralPointResolution;
import org.geocedg.common.kernel.locus.intersection.IntersectionDiagnostic2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionResult2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionSolution2D;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

/**
 * PRE-G9B-CU3 characterization of T7-a (author authorization of 2026-10-26), run only through
 * {@code tools/benchmark/pre-g9b-cu/run-cu3-t7a-characterization.ps1}, with the shadow certificate
 * of that runner on the class path: on a copy of the adopted author model with the public
 * intersections of the E3 bench and the five structural points of the author smoke, a canonical
 * text of every published intersection result and structural point in each of a series of
 * dynamic configurations. The runner compares the texts of the mode {@code compare}, which
 * publishes the certificates of the product, with those of the mode {@code t7a}. It changes
 * nothing in the product, assigns no verdict and is not part of any source set.
 */
@EnabledIfSystemProperty(named = "geocedg.cu0.harness", matches = "cu3-t7a-corpus")
@ExtendWith({G9U1TestApp.Lifecycle.class,
		PreG9BR6PlusD1DocumentUnitsTest.EmptyUnitPreferences.class,
		PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class})
class PreG9BCu3T7aCorpusProbe {

	private static final String[] RESULTS = {"cu3X1=Intersect(e_1,n)",
			"cu3X2=Intersect(e_1,c_1)", "cu3X3=Intersect(e_1,r)", "cu3X4=Intersect(o_1,h_2)"};
	private static final String[] POINTS = {"X1=Intersect(e_1,n,K)", "X2=Intersect(e_1,c_1,K)",
			"X3=Intersect(o_1,h_2,K)", "X4=Intersect(e_1,c_1,J)", "X5=Intersect(e_1,r,J)"};
	private static final String ALPHA = "\u03b1_{cone}";
	private static final String PACKAGES = "org.geocedg.common.kernel.locus.";

	@TempDir
	Path work;

	@Test
	void run() throws Exception {
		Path output = Path.of(System.getProperty("geocedg.cu0.output"));
		Files.createDirectories(output);
		Map<String, Object> root = new LinkedHashMap<>();
		root.put("schema_version", 1);
		root.put("id", "pre-g9b-cu3-t7a-corpus");
		root.put("mode", System.getProperty("geocedg.t7a.mode", "off"));
		root.put("source_commit", System.getProperty("geocedg.cu0.commit"));
		root.put("configurations", PreG9BR6PlusF3Fixtures.onEdt(this::corpus));
		Files.writeString(output.resolve("cu3-t7a-corpus-" + root.get("mode") + ".json"),
				PreG9BCu0References.json(root), StandardCharsets.UTF_8);
	}

	private Map<String, Object> corpus() throws Exception {
		AppGeoCeDG app = PreG9BCu0Corpus.open(PreG9BCu0Corpus.withoutE2(work));
		for (String command : RESULTS) {
			PreG9BR6PlusF3Fixtures.must(app, command);
		}
		Construction construction = app.getKernel().getConstruction();
		context("adoption and points");
		AuthorityPolicyAdoption.adopt(construction);
		for (String command : POINTS) {
			PreG9BR6PlusF3Fixtures.must(app, command);
		}
		Map<String, Object> rows = new LinkedHashMap<>();
		context("base");
		rows.put("base", snapshot(construction));
		for (String label : new String[] {"H", "B"}) {
			double t = PreG9BCu0Corpus.pathParameter(app, label);
			double[] deltas = "H".equals(label) ? new double[] {0.5, -20, 20, 40}
					: new double[] {3, -3};
			for (double delta : deltas) {
				context(label + " " + delta);
				PreG9BCu0Corpus.movePathPoint(app, label, t + delta);
				rows.put(label + " " + delta, snapshot(construction));
			}
			context(label + " restored");
			PreG9BCu0Corpus.movePathPoint(app, label, t);
		}
		GeoPoint d1 = (GeoPoint) construction.lookupLabel("D_1");
		GeoPoint h = (GeoPoint) construction.lookupLabel("H");
		double x = d1.getInhomX();
		double y = d1.getInhomY();
		for (double factor : new double[] {3, 0.5}) {
			context("D_1 x" + factor);
			d1.setCoords(h.getInhomX() + factor * (x - h.getInhomX()),
					h.getInhomY() + factor * (y - h.getInhomY()), 1);
			d1.updateRepaint();
			rows.put("D_1 x" + factor, snapshot(construction));
		}
		context("D_1 restored");
		d1.setCoords(x, y, 1);
		d1.updateRepaint();
		number(app, construction, "rbCone", new double[] {3, 7.5}, rows, 1);
		number(app, construction, ALPHA, new double[] {35, 60}, rows, Math.PI / 180);
		rows.put("restored", snapshot(construction));
		return rows;
	}

	private void number(AppGeoCeDG app, Construction construction, String label,
			double[] values, Map<String, Object> rows, double unit) throws Exception {
		GeoElement number = construction.lookupLabel(label);
		if (number == null) {
			rows.put(label, "absent");
			return;
		}
		double original = ((org.geogebra.common.kernel.geos.GeoNumeric) number).getValue();
		for (double value : values) {
			context(label + "=" + value);
			PreG9BCu0Corpus.setNumber(app, label, value * unit);
			rows.put(label + "=" + value, snapshot(construction));
		}
		context(label + " restored");
		PreG9BCu0Corpus.setNumber(app, label, original);
	}

	/** Names the configuration in the records of the T7-a ledger. */
	private static void context(String name) {
		System.setProperty("geocedg.t7a.context", name);
	}

	/** The canonical text of every intersection result and structural point, by label. */
	private static Map<String, Object> snapshot(Construction construction) throws Exception {
		Map<String, Object> rows = new TreeMap<>();
		for (GeoElement geo : construction.getGeoSetConstructionOrder()) {
			String label = geo.getLabelSimple();
			if (geo instanceof GeoLocusIntersectionResult) {
				GeoLocusIntersectionResult result = (GeoLocusIntersectionResult) geo;
				LocusIntersectionResult2D intersection = result.getIntersectionResult();
				Map<String, Object> row = new LinkedHashMap<>();
				row.put("defined", geo.isDefined());
				if (intersection != null) {
					row.put("status", String.valueOf(intersection.getComputationStatus()));
					row.put("completeness", dump(intersection.getCompletenessEvidence()));
					row.put("unresolved", String.valueOf(
							intersection.getUnresolvedCandidateComponentKeys()));
					row.put("diagnostics", diagnostics(intersection.getDiagnostics()));
					List<Object> solutions = new ArrayList<>();
					for (LocusIntersectionSolution2D solution : intersection.getFiniteSolutions()) {
						Map<String, Object> s = new LinkedHashMap<>();
						s.put("identity", dump(solution.getIdentity()));
						s.put("classification", dump(solution.getClassification()));
						s.put("evidence", dump(solution.getRevisionEvidence()));
						s.put("diagnostics", diagnostics(solution.getDiagnostics()));
						s.put("admissible", result.isPointAdmissible(solution.getIdentity()
								.getRootToken()));
						solutions.add(s);
					}
					row.put("solutions", solutions);
					row.put("work", dump(intersection.getWork()));
				}
				rows.put(label, row);
			} else if (geo.getParentAlgorithm() instanceof AlgoLocusDriverCorrespondencePoint2D) {
				StructuralPointResolution resolution = ((AlgoLocusDriverCorrespondencePoint2D) geo
						.getParentAlgorithm()).getResolution();
				Map<String, Object> row = new LinkedHashMap<>();
				row.put("defined", geo.isDefined());
				if (resolution != null) {
					row.put("integrity", String.valueOf(resolution.getIntegrity()));
					row.put("cause", String.valueOf(resolution.getCause()));
					row.put("x", hex(resolution.getX()));
					row.put("y", hex(resolution.getY()));
					row.put("membership", resolution.getMembership() == null ? "null"
							: String.valueOf(resolution.getMembership().getIntegrity()));
					row.put("incidence", resolution.getIncidence() == null ? "null"
							: String.valueOf(resolution.getIncidence().getIntegrity()));
				}
				rows.put(label, row);
			}
		}
		return rows;
	}

	private static List<String> diagnostics(List<IntersectionDiagnostic2D> diagnostics) {
		List<String> rows = new ArrayList<>();
		for (IntersectionDiagnostic2D diagnostic : diagnostics) {
			rows.add(diagnostic.getCode() + ": " + diagnostic.getMessage());
		}
		return rows;
	}

	/** A canonical text of an object of the intersection model, through its public getters. */
	private static String dump(Object value) throws Exception {
		StringBuilder text = new StringBuilder();
		dump(value, 0, text, new IdentityHashMap<>());
		return text.toString();
	}

	private static void dump(Object value, int depth, StringBuilder text,
			Map<Object, Boolean> seen) throws Exception {
		if (value == null) {
			text.append("null");
		} else if (value instanceof Double || value instanceof Float) {
			text.append(hex(((Number) value).doubleValue()));
		} else if (value instanceof Number || value instanceof Boolean
				|| value instanceof CharSequence || value instanceof Enum) {
			text.append(value);
		} else if (value instanceof double[]) {
			double[] array = (double[]) value;
			text.append('[');
			for (double element : array) {
				text.append(hex(element)).append(',');
			}
			text.append(']');
		} else if (value instanceof Optional) {
			dump(((Optional<?>) value).orElse(null), depth, text, seen);
		} else if (value instanceof Collection) {
			text.append('[');
			for (Object element : (Collection<?>) value) {
				dump(element, depth + 1, text, seen);
				text.append(';');
			}
			text.append(']');
		} else if (value instanceof Map) {
			text.append('{');
			for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
				text.append(entry.getKey()).append('=');
				dump(entry.getValue(), depth + 1, text, seen);
				text.append(';');
			}
			text.append('}');
		} else if (!value.getClass().getName().startsWith(PACKAGES) || depth > 5
				|| seen.put(value, Boolean.TRUE) != null) {
			text.append(value);
		} else {
			Method[] methods = value.getClass().getMethods();
			Arrays.sort(methods, Comparator.comparing(Method::getName));
			text.append(value.getClass().getSimpleName()).append('{');
			for (Method method : methods) {
				String name = method.getName();
				if (method.getParameterCount() != 0 || Modifier.isStatic(method.getModifiers())
						|| !(name.startsWith("get") || name.startsWith("is"))
						|| "getClass".equals(name) || "getWork".equals(name)
						|| "getSourceBinding".equals(name)) {
					continue;
				}
				text.append(name).append('=');
				dump(method.invoke(value), depth + 1, text, seen);
				text.append(';');
			}
			text.append('}');
		}
	}

	private static String hex(double value) {
		return Long.toHexString(Double.doubleToRawLongBits(value));
	}
}
