/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;

import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geocedg.desktop.PreG9BR6ScriptHarness.Sink;
import org.geogebra.common.gui.dialog.options.model.ScriptInputModel;
import org.geogebra.common.kernel.CommandLookupStrategy;
import org.geogebra.common.kernel.Kernel;
import org.geogebra.common.kernel.arithmetic.Command;
import org.geogebra.common.kernel.commands.CommandProcessor;
import org.geogebra.common.kernel.commands.Commands;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.kernelND.GeoElementND;
import org.geogebra.common.move.ggtapi.models.json.JSONArray;
import org.geogebra.common.move.ggtapi.models.json.JSONException;
import org.geogebra.common.move.ggtapi.models.json.JSONObject;
import org.geogebra.common.plugin.EventType;
import org.geogebra.common.plugin.ScriptType;
import org.geogebra.common.plugin.script.GgbScript;
import org.geogebra.common.plugin.script.Script;
import org.geogebra.desktop.main.AppD;
import org.geogebra.desktop.main.undo.UndoManagerD;

/**
 * PRE-G9B-R6 capability matrix: loader, structural completeness gate and row
 * executor. Command identities come from the kernel command enum, processors from
 * the runtime dispatcher, provenance from the upstream modification record and arity
 * forms from the English command bundle; the matrix only references them.
 */
final class PreG9BR6CapabilityMatrix {

	/** Repository path of the matrix instance. */
	static final String INSTANCE =
			"geocedg/validation/pre-g9b-r6/ggbscript-capability-matrix.json";
	/** The executing JUnit class every row names. */
	static final String TEST_CLASS = "org.geocedg.desktop.PreG9BR6CapabilityMatrixTest";
	/** Required entry surfaces. */
	static final List<String> SURFACES = List.of("ALGEBRA_INPUT", "GGBSCRIPT",
			"GGBSCRIPT_EXECUTE");
	/** Required UI locales. */
	static final List<String> LOCALES = List.of("en", "es");

	private static final Map<String, CommandLookupStrategy> STRATEGIES = Map.of(
			"ALGEBRA_INPUT", CommandLookupStrategy.USER,
			"GGBSCRIPT", CommandLookupStrategy.SCRIPT,
			"GGBSCRIPT_EXECUTE", CommandLookupStrategy.XML);
	private static final String MANIFEST = "docs/upstream/modified-files.yml";
	private static final String SYNTAX_BUNDLE =
			"/org/geogebra/common/jre/properties/command.properties";
	private static final String MAIN_SOURCES = "/src/main/java/";
	private static final String MARKER = "org.geocedg.";
	private static final String TOKEN_PLACEHOLDER = "{token:R}";
	private static final long STORE_WAIT_SECONDS = 10;
	private static final long QUIET_WAIT_MILLIS = 1500;

	private PreG9BR6CapabilityMatrix() {
		// utility class
	}

	/** An arity form quoted from the syntax authority. */
	record Form(String key, int index, String line) {
	}

	/** A matrix row with identity (command, form, surface, locale). */
	record Row(String rowId, String commandId, String role, Form form, String surface,
			String locale, String strategy, String fixtureId, String statement,
			String surfaceText, String commandToken, String tokenKind, String authoring,
			String displayName, String stored, String runtimeToken, String verdict,
			Map<String, String> capabilities, String errorKey, String orchestration,
			String junitClass, String junitMethod) {
	}

	/** A lookup-regime probe. */
	record Probe(String probeId, String commandId, String surface, String locale,
			String strategy, String tokenKind, String authoring, String token,
			String fixtureId, String statement, String surfaceText, String verdict,
			String errorKey, String atomicity, String junitClass, String junitMethod) {
	}

	/** A construction the row statement is applied to. */
	record Fixture(String id, List<String> statements, String driver, String trigger) {
	}

	/** A structurally justified exclusion of one arity form. */
	record Exclusion(String commandId, String key, int index, String line, String reason) {
	}

	/** The parsed instance. */
	record Matrix(List<Row> rows, List<Probe> probes, Map<String, Fixture> fixtures,
			List<Exclusion> exclusions, Set<String> processorExclusions,
			List<String> surfaces, List<String> locales) {
	}

	/**
	 * Inventory derived from the current host, never read from the matrix.
	 *
	 * @param roles command identity to GEOCEDG_ADDED or GEOCEDG_MODIFIED
	 * @param syntaxKeys command identity to its syntax key
	 * @param syntax command identity to its syntax lines
	 * @param nonSemanticProcessors registered processors reached at runtime
	 *        without a GeoCeDG-owned reference
	 * @param english command identity to its canonical English head
	 * @param spanish command identity to its Spanish bundle name
	 */
	record Inventory(Map<String, String> roles, Map<String, String> syntaxKeys,
			Map<String, List<String>> syntax, Set<String> nonSemanticProcessors,
			Map<String, String> english, Map<String, String> spanish) {
	}

	// ---------------------------------------------------------------- loading

	static Matrix load() {
		Path file = G9U1AlgebraGestureEditingTest.findRepositoryRoot().resolve(INSTANCE);
		try {
			JSONObject json = new JSONObject(Files.readString(file, StandardCharsets.UTF_8));
			Map<String, Fixture> fixtures = new LinkedHashMap<>();
			for (JSONObject f : objects(json.getJSONArray("fixtures"))) {
				fixtures.put(f.getString("fixture_id"), new Fixture(f.getString("fixture_id"),
						strings(f.getJSONArray("statements")), f.getString("recompute_driver"),
						f.getString("trigger")));
			}
			List<Row> rows = new ArrayList<>();
			for (JSONObject r : objects(json.getJSONArray("rows"))) {
				rows.add(row(r));
			}
			List<Probe> probes = new ArrayList<>();
			for (JSONObject p : objects(json.getJSONArray("lookup_probes"))) {
				probes.add(probe(p));
			}
			List<Exclusion> exclusions = new ArrayList<>();
			for (JSONObject e : objects(json.getJSONArray("form_exclusions"))) {
				exclusions.add(new Exclusion(e.getString("command_id"), e.getString("syntax_key"),
						e.getInt("syntax_index"), e.getString("syntax_line"),
						e.getString("reason")));
			}
			Set<String> processors = new TreeSet<>();
			for (JSONObject e : objects(json.getJSONArray("processor_exclusions"))) {
				processors.add(e.getString("processor_class"));
			}
			List<String> surfaces = new ArrayList<>();
			for (JSONObject s : objects(json.getJSONArray("surfaces"))) {
				surfaces.add(s.getString("surface_id"));
			}
			List<String> locales = new ArrayList<>();
			for (JSONObject l : objects(json.getJSONArray("locales"))) {
				locales.add(l.getString("locale_id"));
			}
			return new Matrix(rows, probes, fixtures, exclusions, processors, surfaces, locales);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		} catch (JSONException e) {
			throw new IllegalStateException(INSTANCE, e);
		}
	}

	private static Row row(JSONObject r) throws JSONException {
		JSONObject form = r.getJSONObject("arity_form");
		JSONObject input = r.getJSONObject("input");
		JSONObject naming = r.getJSONObject("naming");
		JSONObject junit = r.getJSONObject("junit");
		JSONObject capabilities = r.getJSONObject("capabilities");
		Map<String, String> values = new TreeMap<>();
		for (String name : capabilities.keySet()) {
			values.put(name, capabilities.getString(name));
		}
		return new Row(r.getString("row_id"), r.getString("command_id"),
				r.getString("command_role"), new Form(form.getString("syntax_key"),
				form.getInt("syntax_index"), form.getString("syntax_line")),
				r.getString("entry_surface"), r.getString("ui_locale"),
				r.getString("lookup_strategy"), r.getString("fixture_id"),
				input.getString("statement"), input.getString("surface_text"),
				input.getString("command_token"), input.getString("token_kind"),
				input.getString("authoring"), naming.getString("display_name"),
				naming.getString("stored_representation"), naming.getString("runtime_token"),
				r.getString("verdict_type"), Collections.unmodifiableMap(values),
				errorKey(r), r.getString("frontend_orchestration"),
				junit.getString("class"), junit.getString("method"));
	}

	private static Probe probe(JSONObject p) throws JSONException {
		JSONObject junit = p.getJSONObject("junit");
		return new Probe(p.getString("probe_id"), p.getString("command_id"),
				p.getString("entry_surface"), p.getString("ui_locale"),
				p.getString("lookup_strategy"), p.getString("token_kind"),
				p.getString("authoring"), p.getString("token"), p.getString("fixture_id"),
				p.getString("statement"), p.getString("surface_text"),
				p.getString("verdict_type"), errorKey(p), p.getString("refusal_atomicity"),
				junit.getString("class"), junit.getString("method"));
	}

	private static String errorKey(JSONObject owner) throws JSONException {
		if (owner.isNull("expected_error")) {
			return null;
		}
		JSONObject error = owner.getJSONObject("expected_error");
		if (!"ERROR".equals(error.getString("catalog"))) {
			throw new JSONException("unsupported error catalog " + error);
		}
		return error.getString("key");
	}

	private static List<JSONObject> objects(JSONArray array) throws JSONException {
		List<JSONObject> result = new ArrayList<>();
		for (int i = 0; i < array.length(); i++) {
			result.add(array.getJSONObject(i));
		}
		return result;
	}

	private static List<String> strings(JSONArray array) throws JSONException {
		List<String> result = new ArrayList<>();
		for (int i = 0; i < array.length(); i++) {
			result.add(array.getString(i));
		}
		return result;
	}

	// ---------------------------------------------------------------- inventory

	/**
	 * Derives the GeoCeDG command inventory from the running host.
	 *
	 * @param english host in English
	 * @param spanish host in Spanish
	 * @return derived inventory
	 */
	static Inventory derive(AppD english, AppD spanish) {
		Path repository = G9U1AlgebraGestureEditingTest.findRepositoryRoot();
		Map<String, String> provenance = provenance(repository);
		Properties bundle = syntaxBundle();
		Kernel kernel = english.getKernel();
		Map<String, String> roles = new TreeMap<>();
		Map<String, String> syntaxKeys = new TreeMap<>();
		Map<String, List<String>> syntax = new TreeMap<>();
		Map<String, String> heads = new TreeMap<>();
		Map<String, String> aliases = new TreeMap<>();
		Set<String> nonSemantic = new TreeSet<>();
		for (Commands command : Commands.values()) {
			if (Commands.englishToInternal(command) != command) {
				continue; // an English alias constant, not a command identity
			}
			CommandProcessor processor = kernel.getAlgebraProcessor().getCommandDispatcher()
					.commandTableSwitch(new Command(kernel, command.name(), false));
			if (processor == null) {
				continue;
			}
			String role = null;
			for (Class<?> type = processor.getClass(); type != CommandProcessor.class
					&& type != Object.class; type = type.getSuperclass()) {
				String change = provenance.get(type.getName());
				if (change == null) {
					continue;
				}
				String path = change.substring(change.indexOf('|') + 1);
				if (source(repository, path).contains(MARKER)) {
					if (change.startsWith("added|") || role == null) {
						role = change.startsWith("added|") ? "GEOCEDG_ADDED" : "GEOCEDG_MODIFIED";
					}
				} else {
					nonSemantic.add(type.getName());
				}
			}
			if (role == null) {
				continue;
			}
			String id = command.name();
			roles.put(id, role);
			String key = bundle.containsKey(id + ".Syntax3D") ? id + ".Syntax3D"
					: id + ".Syntax";
			syntaxKeys.put(id, key);
			syntax.put(id, List.of(bundle.getProperty(key).split("\n")));
			heads.put(id, english.getLocalization().getCommandHead(id));
			aliases.put(id, spanish.getLocalization().getCommand(id));
		}
		return new Inventory(roles, syntaxKeys, syntax, nonSemantic, heads, aliases);
	}

	private static Map<String, String> provenance(Path repository) {
		try {
			JSONObject manifest = new JSONObject(Files.readString(repository.resolve(MANIFEST),
					StandardCharsets.UTF_8));
			JSONArray modifications = manifest.getJSONArray("modifications");
			Map<String, String> result = new TreeMap<>();
			for (JSONObject entry : objects(modifications)) {
				String path = entry.getString("path");
				String change = entry.getString("change");
				int at = path.indexOf(MAIN_SOURCES);
				if (at >= 0 && path.endsWith(".java")
						&& ("added".equals(change) || "modified".equals(change))) {
					String type = path.substring(at + MAIN_SOURCES.length(), path.length() - 5)
							.replace('/', '.');
					result.put(type, change + "|" + path);
				}
			}
			return result;
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		} catch (JSONException e) {
			throw new IllegalStateException(MANIFEST, e);
		}
	}

	private static String source(Path repository, String path) {
		try {
			return Files.readString(repository.resolve(path), StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private static Properties syntaxBundle() {
		try (InputStream input = PreG9BR6CapabilityMatrix.class
				.getResourceAsStream(SYNTAX_BUNDLE)) {
			if (input == null) {
				throw new IllegalStateException("missing " + SYNTAX_BUNDLE);
			}
			Properties properties = new Properties();
			properties.load(new InputStreamReader(input, StandardCharsets.UTF_8));
			return properties;
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	// ---------------------------------------------------------------- completeness

	/**
	 * Structural completeness: inventory x surfaces x locales x applicable forms.
	 *
	 * @param inventory derived inventory
	 * @param matrix matrix instance
	 * @param junitMethods test methods of the executing class
	 * @return violations; empty when complete
	 */
	static List<String> violations(Inventory inventory, Matrix matrix,
			Set<String> junitMethods) {
		List<String> violations = new ArrayList<>();
		for (String surface : SURFACES) {
			require(violations, matrix.surfaces().contains(surface), "MISSING_SURFACE " + surface);
		}
		for (String locale : LOCALES) {
			require(violations, matrix.locales().contains(locale), "MISSING_LOCALE " + locale);
		}
		require(violations, matrix.processorExclusions().equals(inventory.nonSemanticProcessors()),
				"PROCESSOR_EXCLUSIONS " + matrix.processorExclusions() + " != "
						+ inventory.nonSemanticProcessors());
		Set<String> rowIds = new LinkedHashSet<>();
		Map<String, Set<Integer>> covered = new TreeMap<>();
		Map<String, Integer> cells = new TreeMap<>();
		for (Row row : matrix.rows()) {
			String id = row.commandId();
			require(violations, rowIds.add(row.rowId()), "DUPLICATE_ROW " + row.rowId());
			if (!inventory.roles().containsKey(id)) {
				violations.add("ROW_OUTSIDE_INVENTORY " + row.rowId());
				continue;
			}
			require(violations, row.rowId().equals(id + "#" + row.form().index() + "|"
					+ row.surface() + "|" + row.locale()), "ROW_ID " + row.rowId());
			require(violations, inventory.roles().get(id).equals(row.role()),
					"ROLE " + row.rowId());
			requireForm(violations, inventory, id, row.form().key(), row.form().index(),
					row.form().line(), row.rowId());
			require(violations, SURFACES.contains(row.surface()) && LOCALES.contains(row.locale()),
					"CELL " + row.rowId());
			require(violations, STRATEGIES.get(row.surface()) != null
					&& STRATEGIES.get(row.surface()).name().equals(row.strategy()),
					"STRATEGY " + row.rowId());
			require(violations, inventory.english().get(id).equals(row.displayName())
					&& inventory.english().get(id).equals(row.commandToken())
					&& "CANONICAL_ENGLISH".equals(row.tokenKind()), "NAMING " + row.rowId());
			require(violations, matrix.fixtures().containsKey(row.fixtureId()),
					"FIXTURE " + row.rowId());
			requireVerdict(violations, row);
			require(violations, TEST_CLASS.equals(row.junitClass())
					&& junitMethods.contains(row.junitMethod()), "JUNIT " + row.rowId());
			covered.computeIfAbsent(id, k -> new TreeSet<>()).add(row.form().index());
			cells.merge(id + "#" + row.form().index(), 1, Integer::sum);
		}
		Map<String, Set<Integer>> excluded = new TreeMap<>();
		for (Exclusion exclusion : matrix.exclusions()) {
			String id = exclusion.commandId();
			String label = "EXCLUSION " + id + "#" + exclusion.index();
			if (!inventory.roles().containsKey(id)) {
				violations.add(label + " outside inventory");
				continue;
			}
			require(violations, "GEOCEDG_MODIFIED".equals(inventory.roles().get(id)),
					label + " excludes a form of a GeoCeDG-added command");
			require(violations, "NO_GEOCEDG_BRANCH".equals(exclusion.reason()),
					label + " reason " + exclusion.reason());
			requireForm(violations, inventory, id, exclusion.key(), exclusion.index(),
					exclusion.line(), label);
			require(violations, excluded.computeIfAbsent(id, k -> new TreeSet<>())
					.add(exclusion.index()), label + " duplicate");
		}
		int perForm = SURFACES.size() * LOCALES.size();
		for (Map.Entry<String, List<String>> entry : inventory.syntax().entrySet()) {
			String id = entry.getKey();
			Set<Integer> rows = covered.getOrDefault(id, Set.of());
			Set<Integer> exclusions = excluded.getOrDefault(id, Set.of());
			require(violations, !rows.isEmpty(), "MISSING_COVERAGE " + id);
			for (int index = 1; index <= entry.getValue().size(); index++) {
				boolean row = rows.contains(index);
				boolean exclusion = exclusions.contains(index);
				require(violations, row ^ exclusion, "FORM_UNACCOUNTED " + id + "#" + index
						+ " rows=" + row + " excluded=" + exclusion);
				if (row) {
					require(violations, cells.get(id + "#" + index) == perForm,
							"INCOMPLETE_CELLS " + id + "#" + index + " "
									+ cells.get(id + "#" + index));
				}
			}
		}
		probeViolations(violations, inventory, matrix, junitMethods);
		return violations;
	}

	private static void probeViolations(List<String> violations, Inventory inventory,
			Matrix matrix, Set<String> junitMethods) {
		Set<String> expected = new TreeSet<>();
		for (String id : inventory.roles().keySet()) {
			String head = inventory.english().get(id);
			String alias = inventory.spanish().get(id);
			for (String surface : SURFACES) {
				String authoring = "ALGEBRA_INPUT".equals(surface) ? "TYPED" : "RAW";
				for (String locale : LOCALES) {
					expected.add(id + "|" + surface + "|" + locale + "|CASE_VARIANT|" + authoring);
					if (!head.equals(id)) {
						expected.add(id + "|" + surface + "|" + locale + "|INTERNAL_NAME|"
								+ authoring);
					}
					if (!alias.equals(head)) {
						String kind = "es".equals(locale) ? "LOCALIZED_ALIAS" : "FOREIGN_ALIAS";
						expected.add(id + "|" + surface + "|" + locale + "|" + kind + "|"
								+ authoring);
						if ("es".equals(locale) && !"ALGEBRA_INPUT".equals(surface)) {
							expected.add(id + "|" + surface + "|es|LOCALIZED_ALIAS|SCRIPT_EDITOR");
						}
					}
				}
			}
		}
		Set<String> actual = new TreeSet<>();
		for (Probe probe : matrix.probes()) {
			String id = probe.commandId();
			require(violations, actual.add(probe.probeId()), "DUPLICATE_PROBE " + probe.probeId());
			require(violations, probe.probeId().equals(id + "|" + probe.surface() + "|"
					+ probe.locale() + "|" + probe.tokenKind() + "|" + probe.authoring()),
					"PROBE_ID " + probe.probeId());
			require(violations, STRATEGIES.get(probe.surface()) != null
					&& STRATEGIES.get(probe.surface()).name().equals(probe.strategy()),
					"PROBE_STRATEGY " + probe.probeId());
			require(violations, matrix.fixtures().containsKey(probe.fixtureId()),
					"PROBE_FIXTURE " + probe.probeId());
			require(violations, TEST_CLASS.equals(probe.junitClass())
					&& junitMethods.contains(probe.junitMethod()),
					"PROBE_JUNIT " + probe.probeId());
			boolean refused = "UNSUPPORTED_TRUTHFUL".equals(probe.verdict());
			require(violations, refused == (probe.errorKey() != null)
					&& (refused ? "CONSTRUCTION_XML_IDENTICAL" : "NOT_APPLICABLE")
					.equals(probe.atomicity()) && (refused
					|| "SUPPORTED".equals(probe.verdict())), "PROBE_VERDICT " + probe.probeId());
			if (inventory.roles().containsKey(id)) {
				require(violations, probe.token().equals(expectedToken(inventory, probe)),
						"PROBE_TOKEN " + probe.probeId());
			}
		}
		Set<String> missing = new TreeSet<>(expected);
		missing.removeAll(actual);
		Set<String> extra = new TreeSet<>(actual);
		extra.removeAll(expected);
		require(violations, missing.isEmpty(), "MISSING_PROBES " + missing);
		require(violations, extra.isEmpty(), "UNEXPECTED_PROBES " + extra);
	}

	private static String expectedToken(Inventory inventory, Probe probe) {
		switch (probe.tokenKind()) {
		case "CASE_VARIANT":
			return inventory.english().get(probe.commandId()).toLowerCase(Locale.ROOT);
		case "INTERNAL_NAME":
			return probe.commandId();
		default:
			return inventory.spanish().get(probe.commandId());
		}
	}

	private static void requireForm(List<String> violations, Inventory inventory, String id,
			String key, int index, String line, String label) {
		List<String> lines = inventory.syntax().get(id);
		require(violations, inventory.syntaxKeys().get(id).equals(key), "SYNTAX_KEY " + label);
		require(violations, index >= 1 && index <= lines.size()
				&& lines.get(index - 1).equals(line), "SYNTAX_LINE " + label);
	}

	private static void requireVerdict(List<String> violations, Row row) {
		Map<String, String> expected = new TreeMap<>();
		boolean supported = "SUPPORTED".equals(row.verdict());
		expected.put("name_lookup", "RESOLVED");
		expected.put("processor_dispatch", supported ? "DISPATCHED" : "REFUSED");
		expected.put("dag_construction", supported ? "CONSTRUCTED" : "NOT_APPLICABLE");
		expected.put("recompute", supported ? "RECOMPUTED" : "NOT_APPLICABLE");
		expected.put("undo", supported ? "SINGLE_STEP_RESTORES_PRE_STATEMENT" : "NO_UNDO_STEP");
		expected.put("native_persistence", supported ? "ROUND_TRIP_IDENTICAL" : "NOT_APPLICABLE");
		expected.put("refusal_atomicity", supported ? "NOT_APPLICABLE"
				: "CONSTRUCTION_XML_IDENTICAL");
		require(violations, supported || "UNSUPPORTED_TRUTHFUL".equals(row.verdict()),
				"VERDICT_TYPE " + row.rowId());
		require(violations, expected.equals(row.capabilities()), "CAPABILITIES " + row.rowId());
		require(violations, supported == (row.errorKey() == null), "ERROR_KEY " + row.rowId());
		String orchestration = !"ALGEBRA_INPUT".equals(row.surface()) ? "NOT_INHERITED"
				: !supported ? "NOT_REACHED" : null;
		require(violations, orchestration == null ? Set.of("RICH_RESULT_SESSION_ACTIVATED",
				"HOST_PATH_WITHOUT_RICH_RESULT").contains(row.orchestration())
				: orchestration.equals(row.orchestration()), "ORCHESTRATION " + row.rowId());
	}

	private static void require(List<String> violations, boolean condition, String violation) {
		if (!condition) {
			violations.add(violation);
		}
	}

	// ---------------------------------------------------------------- execution

	/** One UI language: the authoring host and a separate host that reopens documents. */
	static final class Host implements AutoCloseable {
		private final AppGeoCeDG app;
		private final AppGeoCeDG reopened;
		private final Sink sink = new Sink();
		private final Path directory;
		private final List<AutoCloseable> restore = new ArrayList<>();
		private final AtomicReference<CountDownLatch> store =
				new AtomicReference<>(new CountDownLatch(1));
		private final List<GeoElement> updates = Collections.synchronizedList(new ArrayList<>());

		/**
		 * @param locale UI locale identifier
		 * @param directory temporary document directory
		 * @throws ReflectiveOperationException if the presentation seam is unavailable
		 */
		Host(String locale, Path directory) throws ReflectiveOperationException {
			Locale language = Locale.forLanguageTag(locale);
			this.app = PreG9BR6ScriptHarness.application(language);
			this.reopened = PreG9BR6ScriptHarness.application(language);
			this.directory = directory;
			restore.add(PreG9BR6ScriptHarness.install(app, sink));
			restore.add(PreG9BR6ScriptHarness.install(reopened, sink));
			undo(app).addUndoInfoStoredListener(() -> store.get().countDown());
			for (AppGeoCeDG host : List.of(app, reopened)) {
				host.getEventDispatcher().addEventListener(event -> {
					if (event.type == EventType.UPDATE && event.target != null) {
						updates.add(event.target);
					}
				});
			}
		}

		AppGeoCeDG app() {
			return app;
		}

		@Override
		public void close() throws Exception {
			for (AutoCloseable closeable : restore) {
				closeable.close();
			}
		}
	}

	/**
	 * Executes one row on its surface and checks every declared capability.
	 *
	 * @param host host of the row's locale
	 * @param matrix matrix instance
	 * @param row row
	 * @throws Exception on host failure
	 */
	static void execute(Host host, Matrix matrix, Row row) throws Exception {
		AppGeoCeDG app = host.app;
		Fixture fixture = matrix.fixtures().get(row.fixtureId());
		GeoElement trigger = prepare(host, fixture, row.rowId());
		String statement = resolveToken(app, row.statement());
		String surfaceText = resolveToken(app, row.surfaceText());
		String stored = resolveToken(app, row.stored());
		check(app.getLocalization().getCommandHead(row.commandId()).equals(row.displayName()),
				row, "display name");
		checkLookup(app, row);
		if (!"ALGEBRA_INPUT".equals(row.surface())) {
			Script script = author(app, trigger, surfaceText);
			check(stored.equals(script.getInternalText()), row,
					"stored script " + script.getInternalText());
			check(surfaceText.equals(script.getText()), row, "editor display " + script.getText());
		}
		baseline(app);
		CountDownLatch stored0 = new CountDownLatch(1);
		host.store.set(stored0);
		String before = PreG9BR6ScriptHarness.constructionXml(app);
		run(host, row.surface(), statement, trigger);
		GeoElement output = app.getKernel().lookupLabel("X");
		if ("UNSUPPORTED_TRUTHFUL".equals(row.verdict())) {
			check(output == null, row, "refused statement created an output");
			check(before.equals(PreG9BR6ScriptHarness.constructionXml(app)), row,
					"refusal mutated the construction XML");
			checkError(app, host.sink, row.errorKey(), row.rowId());
			check(!stored0.await(QUIET_WAIT_MILLIS, TimeUnit.MILLISECONDS), row,
					"refusal stored an undo step");
			checkOrchestration(app, row, null);
			return;
		}
		check(output != null, row, "no output: " + host.sink.joined());
		Commands identity = Commands.valueOf(row.commandId());
		check(output.getParentAlgorithm() != null
				&& output.getParentAlgorithm().getClassName() == identity, row,
				"dispatch identity");
		if ("ALGEBRA_INPUT".equals(row.surface())) {
			XmlCommand.require(PreG9BR6ScriptHarness.constructionXml(app), row.stored(), row);
		}
		GeoElement driver = app.getKernel().lookupLabel(fixture.driver());
		check(driver != null && output.isChildOf(driver), row, "DAG dependency on the driver");
		checkOrchestration(app, row, output);
		String created = PreG9BR6ScriptHarness.constructionXml(app);
		checkRecompute(host, app, row, fixture, created);
		checkPersistence(host, row, fixture, created);
		check(stored0.await(STORE_WAIT_SECONDS, TimeUnit.SECONDS), row, "no undo step stored");
		app.getKernel().undo();
		await(() -> before.equals(PreG9BR6ScriptHarness.constructionXml(app)), row,
				"one undo step does not restore the pre-statement construction");
		check(app.getKernel().lookupLabel("X") == null, row, "undo kept the output");
	}

	/**
	 * Executes one lookup probe.
	 *
	 * @param host host of the probe's locale
	 * @param matrix matrix instance
	 * @param probe probe
	 * @throws Exception on host failure
	 */
	static void execute(Host host, Matrix matrix, Probe probe) throws Exception {
		AppGeoCeDG app = host.app;
		Fixture fixture = matrix.fixtures().get(probe.fixtureId());
		GeoElement trigger = prepare(host, fixture, probe.probeId());
		if ("SCRIPT_EDITOR".equals(probe.authoring())) {
			author(app, trigger, probe.surfaceText());
		} else if ("RAW".equals(probe.authoring())) {
			trigger.setClickScript(new GgbScript(app, probe.surfaceText()));
		}
		String before = PreG9BR6ScriptHarness.constructionXml(app);
		run(host, probe.surface(), probe.statement(), trigger);
		GeoElement output = app.getKernel().lookupLabel("X");
		String label = probe.probeId();
		if ("SUPPORTED".equals(probe.verdict())) {
			checkThat(output != null && output.getParentAlgorithm() != null
					&& output.getParentAlgorithm().getClassName()
					== Commands.valueOf(probe.commandId()), label, "no output of the command: "
					+ host.sink.joined());
		} else {
			checkThat(output == null, label, "refused token created an output");
			checkThat(before.equals(PreG9BR6ScriptHarness.constructionXml(app)), label,
					"refusal mutated the construction XML");
			checkError(app, host.sink, probe.errorKey(), label);
		}
	}

	private static GeoElement prepare(Host host, Fixture fixture, String label)
			throws Exception {
		AppGeoCeDG app = host.app;
		app.setSaved();
		app.clearConstruction();
		host.sink.clear();
		Kernel kernel = app.getKernel();
		for (String statement : fixture.statements()) {
			GeoElementND[] created = kernel.getAlgebraProcessor()
					.processAlgebraCommandNoExceptionHandling(statement, false, host.sink,
							false, null);
			checkThat(created != null, label, "fixture statement " + statement + ": "
					+ host.sink.joined());
		}
		GeoElementND[] trigger = kernel.getAlgebraProcessor()
				.processAlgebraCommandNoExceptionHandling(fixture.trigger(), false, host.sink,
						false, null);
		checkThat(trigger != null && host.sink.messages().isEmpty(), label,
				"fixture: " + host.sink.joined());
		return trigger[0].toGeoElement();
	}

	private static Script author(AppGeoCeDG app, GeoElement trigger, String text) {
		ScriptInputModel model = new ScriptInputModel(app, EventType.CLICK, "OnClick");
		model.setGeos(new Object[] {trigger});
		model.processInput(text, ScriptType.GGBSCRIPT);
		return trigger.getScript(EventType.CLICK);
	}

	private static void run(Host host, String surface, String statement, GeoElement trigger)
			throws Exception {
		host.sink.clear();
		if ("ALGEBRA_INPUT".equals(surface)) {
			PreG9BR6ScriptHarness.submit(host.app, statement, host.sink);
		} else {
			trigger.runClickScripts(null);
		}
	}

	private static void baseline(AppGeoCeDG app) throws IOException {
		UndoManagerD undo = undo(app);
		try (var baseline = undo.prepareUndoBaseline()) {
			undo.commitUndoBaseline(baseline);
		}
	}

	private static UndoManagerD undo(AppD app) {
		return (UndoManagerD) app.getKernel().getConstruction().getUndoManager();
	}

	private static String resolveToken(AppGeoCeDG app, String text) {
		if (!text.contains(TOKEN_PLACEHOLDER)) {
			return text;
		}
		GeoLocusIntersectionResult result =
				(GeoLocusIntersectionResult) app.getKernel().lookupLabel("R");
		String token = result.getIntersectionResult().getFiniteSolutions().stream()
				.map(solution -> solution.getIdentity().getRootToken()).sorted()
				.findFirst().orElseThrow();
		return text.replace(TOKEN_PLACEHOLDER, token);
	}

	private static void checkLookup(AppGeoCeDG app, Row row) {
		Kernel kernel = app.getKernel();
		String token = "GGBSCRIPT".equals(row.surface()) ? head(row.stored())
				: row.commandToken();
		CommandLookupStrategy previous = kernel.getCommandLookupStrategy();
		kernel.setCommandLookupStrategy(CommandLookupStrategy.valueOf(row.strategy()));
		String name;
		try {
			name = new Command(kernel, token, true).getName();
		} finally {
			kernel.setCommandLookupStrategy(previous);
		}
		check(row.runtimeToken().equals(name), row, "runtime token " + name);
		check(Commands.englishToInternal(Commands.valueOf(name))
				== Commands.valueOf(row.commandId()), row, "lookup identity");
	}

	private static String head(String statement) {
		return statement.substring(statement.indexOf('=') + 1, statement.indexOf('('));
	}

	private static void checkOrchestration(AppGeoCeDG app, Row row, GeoElement output) {
		GeoCeDGEuclidianController controller =
				(GeoCeDGEuclidianController) app.getEuclidianView1().getEuclidianController();
		check(!controller.isAutoMaterializeIntersectionSolutions(), row,
				"auto-materialize is on by default");
		GeoLocusIntersectionResult active = controller.getIntersectionSession().getActive();
		boolean activated = active != null && active == output;
		check(activated == "RICH_RESULT_SESSION_ACTIVATED".equals(row.orchestration()), row,
				"frontend orchestration " + activated);
	}

	private static void checkRecompute(Host host, AppGeoCeDG app, Row row, Fixture fixture,
			String created) {
		GeoElement output = app.getKernel().lookupLabel("X");
		host.updates.clear();
		Runnable restore = perturb(host, app, fixture);
		check(host.updates.contains(output), row, "the output did not recompute");
		restore.run();
		String restored = PreG9BR6ScriptHarness.constructionXml(app);
		check(created.equals(restored), row,
				"restoring the driver did not restore the construction " + difference(created,
						restored));
	}

	private static String difference(String expected, String actual) {
		int at = 0;
		while (at < Math.min(expected.length(), actual.length())
				&& expected.charAt(at) == actual.charAt(at)) {
			at++;
		}
		int from = Math.max(0, at - 60);
		return "at " + at + ": expected ..." + expected.substring(from,
				Math.min(expected.length(), at + 80)) + "... actual ..."
				+ actual.substring(from, Math.min(actual.length(), at + 80)) + "...";
	}

	private static void checkPersistence(Host host, Row row, Fixture fixture,
			String created) throws Exception {
		Path file = host.directory.resolve(row.rowId().replaceAll("[^A-Za-z0-9]+", "_")
				+ ".cedg");
		check(((GuiManagerGeoCeDG) host.app.getGuiManager()).saveAsTo(file.toFile()), row,
				"native save");
		host.reopened.setSaved();
		check(host.reopened.loadFile(file.toFile(), false), row, "native reopen");
		check(created.equals(PreG9BR6ScriptHarness.constructionXml(host.reopened)), row,
				"reopened construction XML differs");
		GeoElement output = host.reopened.getKernel().lookupLabel("X");
		check(output != null && output.getParentAlgorithm() != null
				&& output.getParentAlgorithm().getClassName() == Commands.valueOf(row.commandId()),
				row, "reopened command identity");
		host.updates.clear();
		Runnable restore = perturb(host, host.reopened, fixture);
		check(host.updates.contains(output), row, "the reopened output did not recompute");
		restore.run();
		check(created.equals(PreG9BR6ScriptHarness.constructionXml(host.reopened)), row,
				"restoring the reopened driver did not restore the construction");
	}

	/**
	 * Moves the fixture's free driver by an ordinary redefinition and returns the
	 * action that re-enters its fixture definition; no value is asserted.
	 */
	private static Runnable perturb(Host host, AppGeoCeDG app, Fixture fixture) {
		String label = fixture.driver();
		String definition = null;
		for (String statement : fixture.statements()) {
			if (statement.startsWith(label + "=")) {
				definition = statement;
			}
		}
		checkThat(definition != null, fixture.id(), "driver definition");
		GeoElement driver = app.getKernel().lookupLabel(label);
		String moved;
		if (driver instanceof GeoNumeric) {
			moved = label + "=" + (((GeoNumeric) driver).getValue() + 0.25);
		} else {
			GeoPoint point = (GeoPoint) driver;
			moved = label + "=(" + (point.getInhomX() + 0.25) + ","
					+ (point.getInhomY() + 0.125) + ")";
		}
		redefine(host, app, moved, fixture.id());
		String original = definition;
		return () -> redefine(host, app, original, fixture.id());
	}

	private static void redefine(Host host, AppGeoCeDG app, String statement, String label) {
		GeoElementND[] result = app.getKernel().getAlgebraProcessor()
				.processAlgebraCommandNoExceptionHandling(statement, false, host.sink, false,
						null);
		checkThat(result != null, label, "driver redefinition " + statement + ": "
				+ host.sink.joined());
	}

	private static void checkError(AppGeoCeDG app, Sink sink, String key, String label) {
		String text = app.getLocalization().getError(key);
		checkThat(!text.equals(key) && sink.joined().contains(text), label,
				"expected error " + key + " (" + text + ") in: " + sink.joined());
	}

	private static void await(BooleanSupplier condition, Row row, String message)
			throws InterruptedException {
		long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(STORE_WAIT_SECONDS);
		while (!condition.getAsBoolean() && System.nanoTime() < deadline) {
			Thread.sleep(10);
		}
		check(condition.getAsBoolean(), row, message);
	}

	private static void check(boolean condition, Row row, String message) {
		checkThat(condition, row.rowId(), message);
	}

	private static void checkThat(boolean condition, String label, String message) {
		if (!condition) {
			throw new AssertionError(label + ": " + message);
		}
	}

	/** Finds a command element of the construction XML by its stored command name. */
	static final class XmlCommand {
		private XmlCommand() {
		}

		static void require(String xml, String name, Row row) {
			check(xml.contains("<command name=\"" + name + "\""), row,
					"stored command name " + name);
		}
	}

	/**
	 * @param inventory derived inventory
	 * @param commandId added command identity
	 * @param lines its syntax lines
	 * @return the inventory extended by a synthetic GeoCeDG-added command
	 */
	static Inventory withAddedCommand(Inventory inventory, String commandId,
			List<String> lines) {
		Map<String, String> roles = new TreeMap<>(inventory.roles());
		roles.put(commandId, "GEOCEDG_ADDED");
		Map<String, String> keys = new TreeMap<>(inventory.syntaxKeys());
		keys.put(commandId, commandId + ".Syntax");
		Map<String, List<String>> syntax = new TreeMap<>(inventory.syntax());
		syntax.put(commandId, lines);
		Map<String, String> english = new TreeMap<>(inventory.english());
		english.put(commandId, commandId);
		Map<String, String> spanish = new TreeMap<>(inventory.spanish());
		spanish.put(commandId, commandId);
		return new Inventory(roles, keys, syntax, inventory.nonSemanticProcessors(),
				english, spanish);
	}

	/**
	 * @param inventory derived inventory
	 * @param commandId existing command identity
	 * @param line syntax line appended to its form list
	 * @return the inventory with one more arity form
	 */
	static Inventory withExtraForm(Inventory inventory, String commandId, String line) {
		Map<String, List<String>> syntax = new TreeMap<>(inventory.syntax());
		List<String> lines = new ArrayList<>(syntax.get(commandId));
		lines.add(line);
		syntax.put(commandId, lines);
		return new Inventory(inventory.roles(), inventory.syntaxKeys(), syntax,
				inventory.nonSemanticProcessors(), inventory.english(), inventory.spanish());
	}

	/**
	 * @param matrix matrix instance
	 * @param rowId row to drop
	 * @return the matrix without that row
	 */
	static Matrix withoutRow(Matrix matrix, String rowId) {
		List<Row> rows = new ArrayList<>(matrix.rows());
		rows.removeIf(row -> row.rowId().equals(rowId));
		return new Matrix(rows, matrix.probes(), matrix.fixtures(), matrix.exclusions(),
				matrix.processorExclusions(), matrix.surfaces(), matrix.locales());
	}

	/**
	 * @param matrix matrix instance
	 * @param exclusion additional exclusion
	 * @return the matrix with that exclusion
	 */
	static Matrix withExclusion(Matrix matrix, Exclusion exclusion) {
		List<Exclusion> exclusions = new ArrayList<>(matrix.exclusions());
		exclusions.add(exclusion);
		return new Matrix(matrix.rows(), matrix.probes(), matrix.fixtures(), exclusions,
				matrix.processorExclusions(), matrix.surfaces(), matrix.locales());
	}

	/**
	 * @param rows rows
	 * @param method JUnit method
	 * @return the rows owned by that method
	 */
	static List<Row> rowsOf(List<Row> rows, String method) {
		List<Row> owned = new ArrayList<>();
		for (Row row : rows) {
			if (row.junitMethod().equals(method)) {
				owned.add(row);
			}
		}
		return owned;
	}

	/**
	 * @param probes probes
	 * @param method JUnit method
	 * @return the probes owned by that method
	 */
	static List<Probe> probesOf(List<Probe> probes, String method) {
		List<Probe> owned = new ArrayList<>();
		for (Probe probe : probes) {
			if (probe.junitMethod().equals(method)) {
				owned.add(probe);
			}
		}
		return owned;
	}

	/**
	 * @param values values
	 * @return a stable set view
	 */
	static Set<String> setOf(String... values) {
		return new TreeSet<>(Arrays.asList(values));
	}
}
