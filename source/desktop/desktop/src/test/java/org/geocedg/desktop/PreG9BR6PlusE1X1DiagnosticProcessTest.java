/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import javax.swing.SwingUtilities;

import org.geogebra.common.io.XMLStringBuilder;
import org.geogebra.common.main.App;
import org.geogebra.desktop.gui.app.GeoGebraFrame;
import org.geogebra.desktop.main.AppD;
import org.geogebra.desktop.main.GeoGebraPreferencesD;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * PRE-G9B-R6-plus-E1-X1 process tests: real JVMs, real windows, no pipes.
 *
 * <p>The first test starts the real entry point {@code GeoCeDG.main} with the
 * private dispatch marker, as a packaged {@code GeoCeDG.exe} child is started:
 * the process must be genuine Classic, created by nothing of GeoCeDG, with the
 * isolated Classic preferences, and must close by itself. The second test starts
 * a real GeoCeDG application whose diagnostic routes start real Classic
 * children (the development route, as in a Gradle or IDE launch): the
 * Laboratory with the verbose {@code Templatev7.ggb} must reach its window, close
 * by itself and leave the parent untouched; Open Classic must survive the
 * parent. An observer agent, given to the JVMs through {@code JAVA_TOOL_OPTIONS},
 * reports from inside each Classic child; it acts only in Classic JVMs. Every
 * JVM writes to a file, uses a scratch {@code APPDATA}, and every recorded
 * process is destroyed at the end.
 */
class PreG9BR6PlusE1X1DiagnosticProcessTest {

	static final String PROBE_DIRECTORY = "GEOCEDG_E1X1_PROBE_DIR";
	private static final String MARKER = "--classic-diagnostic";
	private static final long JVM_TIMEOUT_SECONDS = 300;
	private static final long REPORT_TIMEOUT_SECONDS = 150;
	/** product classes that must never be loaded in a diagnostic Classic JVM */
	private static final Set<String> GEOCEDG_PRODUCT_CLASSES = Set.of(
			"org.geocedg.desktop.AppGeoCeDG", "org.geocedg.desktop.GeoCeDGFrame",
			"org.geocedg.desktop.GeoCeDGProfile", "org.geocedg.desktop.GeoCeDGActionRegistry");

	/**
	 * Managed by hand: right after the JVMs end, Windows may still hold the agent
	 * jar for a moment, which would fail a JUnit {@code @TempDir} cleanup.
	 */
	private Path temporary;

	@BeforeEach
	void createTemporaryDirectory() throws IOException {
		temporary = Files.createTempDirectory("geocedg-e1x1-process-");
	}

	@AfterEach
	void deleteTemporaryDirectory() throws Exception {
		for (int attempt = 0; attempt < 20 && Files.exists(temporary); attempt++) {
			try (var paths = Files.walk(temporary)) {
				for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
					Files.deleteIfExists(path);
				}
			} catch (IOException stillInUse) {
				Thread.sleep(500);
			}
		}
	}

	@Test
	void dispatchStartsGenuineClassicInItsOwnProcessWithIsolatedDefaults()
			throws Exception {
		Path appData = Files.createDirectories(temporary.resolve("appdata"));
		Path probes = Files.createDirectories(temporary.resolve("probes"));
		Files.writeString(probes.resolve("exit-mode"), "exit");
		Path console = temporary.resolve("dispatch-console.txt");
		Process process = startJvm(GeoCeDG.class.getName(), List.of(MARKER), appData, probes,
				console);
		List<Long> started = new ArrayList<>(List.of(process.pid()));
		try {
			boolean ended = process.waitFor(JVM_TIMEOUT_SECONDS, TimeUnit.SECONDS);
			List<Properties> reports = reports(probes);
			String detail = describe(reports, console);
			// kept in the JUnit report as evidence
			System.out.println("E1-X1 dispatch: test JVM " + ProcessHandle.current().pid()
					+ ", Classic JVM " + process.pid() + ", reports " + reports);
			assertTrue(ended, "the dispatched Classic JVM closed by itself\n" + detail);
			assertEquals(1, reports.size(), detail);
			Properties classic = reports.get(0);
			assertEquals("true", classic.getProperty("ok"), detail);
			assertEquals(Long.toString(process.pid()), classic.getProperty("pid"), detail);
			assertNotEquals(Long.toString(ProcessHandle.current().pid()),
					classic.getProperty("pid"), "a separate process");
			assertGenuineClassic(classic, detail);
			Path diagnosticPreferences = appData.resolve("GeoCeDG").resolve("5.4")
					.resolve("classic-diagnostic.properties");
			// a bare marker gets the isolated Classic preferences
			assertEquals(diagnosticPreferences.toString(), classic.getProperty("settings"),
					detail);
			assertEquals("", classic.getProperty("currentFile"), detail);
			assertEquals(0, process.exitValue(), "Classic exit\n" + detail);
			assertTrue(Files.isRegularFile(diagnosticPreferences),
					"Classic wrote its own preferences at exit\n" + detail);
			assertFalse(Files.exists(diagnosticPreferences.resolveSibling(
					"preferences.properties")), "the dispatch never touched the GeoCeDG"
					+ " preferences\n" + detail);
		} finally {
			destroy(started, probes);
		}
	}

	@Test
	void realGeoCeDGParentStartsSeparateClassicProcesses() throws Exception {
		Path appData = Files.createDirectories(temporary.resolve("appdata"));
		Path probes = Files.createDirectories(temporary.resolve("probes"));
		Path model = Files.createDirectories(temporary.resolve("models"))
				.resolve("Templatev7.ggb");
		Files.copy(repositoryRoot().resolve("models").resolve("legacy")
				.resolve("template-v7").resolve("original").resolve("Templatev7.ggb"), model);
		Path result = temporary.resolve("parent-result.properties");
		Path console = temporary.resolve("parent-console.txt");
		Process parent = startJvm(ParentDriver.class.getName(),
				List.of(model.toString(), result.toString()), appData, probes, console);
		List<Long> started = new ArrayList<>(List.of(parent.pid()));
		try {
			boolean ended = parent.waitFor(JVM_TIMEOUT_SECONDS, TimeUnit.SECONDS);
			List<Properties> reports = reports(probes);
			Properties driver = load(result);
			String detail = "parent: " + driver + "\n" + describe(reports, console);
			// kept in the JUnit report as evidence
			System.out.println("E1-X1 parent: " + driver + "\nE1-X1 Classic children: "
					+ reports);
			assertTrue(ended, "the GeoCeDG parent closed\n" + detail);
			assertEquals("true", driver.getProperty("ok"), detail);
			String parentPid = Long.toString(parent.pid());
			assertEquals(parentPid, driver.getProperty("parentPid"), detail);
			assertEquals("org.geocedg.desktop.AppGeoCeDG", driver.getProperty("parentApp"),
					detail);
			assertEquals(2, reports.size(), detail);
			Properties laboratory = report(reports, driver.getProperty("laboratoryPid"));
			Properties classic = report(reports, driver.getProperty("classicPid"));
			Path preferenceDirectory = appData.resolve("GeoCeDG").resolve("5.4");
			for (Properties child : List.of(laboratory, classic)) {
				assertEquals("true", child.getProperty("ok"), detail);
				assertEquals(parentPid, child.getProperty("parentPid"), detail);
				assertNotEquals(parentPid, child.getProperty("pid"), detail);
				assertGenuineClassic(child, detail);
				assertTrue(child.getProperty("moduleAccess").contains(
						"java.desktop/sun.awt=ALL-UNNAMED"),
						"the development child inherits the module-access options\n" + detail);
			}
			assertNotEquals(laboratory.getProperty("pid"), classic.getProperty("pid"));
			// Templatev7 reached its window although Classic logs it verbosely
			assertEquals(model.toString(), laboratory.getProperty("currentFile"), detail);
			assertEquals("23", laboratory.getProperty("constructionSteps"), detail);
			assertEquals(preferenceDirectory.resolve("laboratory.properties").toString(),
					laboratory.getProperty("settings"), detail);
			assertEquals("", classic.getProperty("currentFile"),
					"the active GeoCeDG document is not forwarded\n" + detail);
			assertEquals(preferenceDirectory.resolve("classic-diagnostic.properties")
					.toString(), classic.getProperty("settings"), detail);
			// the Laboratory child closed by itself; the parent survived it untouched
			assertEquals("true", driver.getProperty("laboratoryExitedByItself"), detail);
			assertEquals("true", driver.getProperty("parentResponsiveAfterChildExit"),
					detail);
			assertEquals("true", driver.getProperty("constructionUnchanged"), detail);
			assertEquals("true", driver.getProperty("savedStateUnchanged"), detail);
			assertEquals("true", driver.getProperty("currentFileUnchanged"), detail);
			assertEquals("true", driver.getProperty("mainPreferencesUnchanged"), detail);
			assertEquals(3, Set.of(driver.getProperty("mainPreferences"),
					laboratory.getProperty("settings"), classic.getProperty("settings")).size(),
					"three distinct preference stores\n" + detail);
			// the parent closed; the Open Classic child survived it
			long classicPid = Long.parseLong(classic.getProperty("pid"));
			started.add(classicPid);
			Optional<ProcessHandle> survivor = ProcessHandle.of(classicPid);
			assertTrue(survivor.isPresent() && survivor.get().isAlive(),
					"the Classic child survives its parent\n" + detail);
		} finally {
			destroy(started, probes);
		}
	}

	private static void assertGenuineClassic(Properties child, String detail) {
		assertEquals("org.geogebra.desktop.geogebra3D.App3D", child.getProperty("app"),
				detail);
		assertEquals("org.geogebra.common.main.settings.config.AppConfigDefault",
				child.getProperty("config"), detail);
		assertEquals("classic", child.getProperty("appCode"), detail);
		assertEquals("org.geogebra.desktop.geogebra3D.gui.GuiManager3D",
				child.getProperty("guiManager"), detail);
		assertEquals("org.geogebra.desktop.gui.app.GeoGebraFrame3D", child.getProperty(
				"frames"), detail);
		assertEquals("", child.getProperty("geocedgProductClassesLoaded"), detail);
	}

	private Process startJvm(String mainClass, List<String> arguments, Path appData,
			Path probes, Path console) throws IOException {
		List<String> command = new ArrayList<>(List.of(
				ProcessHandle.current().info().command().orElseThrow(),
				"--add-exports", "java.base/java.lang=ALL-UNNAMED",
				"--add-exports", "java.desktop/sun.awt=ALL-UNNAMED",
				"--add-exports", "java.desktop/sun.java2d=ALL-UNNAMED",
				"-cp", System.getProperty("java.class.path"), mainClass));
		command.addAll(arguments);
		ProcessBuilder builder = new ProcessBuilder(command).redirectErrorStream(true)
				.redirectOutput(console.toFile());
		String agent = observerAgent().toString();
		builder.environment().put("JAVA_TOOL_OPTIONS",
				agent.contains(" ") ? "\"-javaagent:" + agent + "\"" : "-javaagent:" + agent);
		builder.environment().put("APPDATA", appData.toString());
		builder.environment().put(PROBE_DIRECTORY, probes.toString());
		return builder.start();
	}

	/** a manifest-only agent jar; its premain class comes from the class path */
	private Path observerAgent() throws IOException {
		Path jar = temporary.resolve("e1x1-observer-agent.jar");
		if (!Files.exists(jar)) {
			Manifest manifest = new Manifest();
			manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
			manifest.getMainAttributes().put(new Attributes.Name("Premain-Class"),
					Observer.class.getName());
			try (OutputStream file = Files.newOutputStream(jar);
					JarOutputStream ignored = new JarOutputStream(file, manifest)) {
				// no entries
			}
		}
		return jar;
	}

	private static List<Properties> reports(Path probes) throws IOException {
		List<Properties> reports = new ArrayList<>();
		try (var files = Files.list(probes)) {
			for (Path file : files.sorted().toList()) {
				if (file.getFileName().toString().startsWith("classic-")
						&& file.getFileName().toString().endsWith(".properties")) {
					reports.add(load(file));
				}
			}
		}
		return reports;
	}

	private static Properties report(List<Properties> reports, String pid) {
		for (Properties report : reports) {
			if (report.getProperty("pid").equals(pid)) {
				return report;
			}
		}
		throw new AssertionError("no Classic report for pid " + pid + ": " + reports);
	}

	static Properties load(Path file) throws IOException {
		Properties properties = new Properties();
		if (Files.exists(file)) {
			try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
				properties.load(reader);
			}
		}
		return properties;
	}

	static void store(Properties properties, Path file) throws IOException {
		Path partial = file.resolveSibling(file.getFileName() + ".partial");
		try (Writer writer = Files.newBufferedWriter(partial, StandardCharsets.UTF_8)) {
			properties.store(writer, "PRE-G9B-R6-plus-E1-X1");
		}
		Files.move(partial, file, StandardCopyOption.REPLACE_EXISTING,
				StandardCopyOption.ATOMIC_MOVE);
	}

	private static String describe(List<Properties> reports, Path console)
			throws IOException {
		StringBuilder text = new StringBuilder("Classic reports: ").append(reports);
		if (Files.exists(console)) {
			List<String> lines = Files.readAllLines(console, StandardCharsets.ISO_8859_1);
			text.append("\n--- console (tail) ---\n").append(String.join("\n",
					lines.subList(Math.max(0, lines.size() - 40), lines.size())));
		}
		return text.toString();
	}

	/** destroys every started JVM, its descendants and every reported Classic child */
	private static void destroy(List<Long> started, Path probes) throws Exception {
		Set<Long> pids = new TreeSet<>(started);
		for (Properties report : reports(probes)) {
			pids.add(Long.parseLong(report.getProperty("pid")));
		}
		for (long pid : pids) {
			ProcessHandle.of(pid).ifPresent(handle -> {
				handle.descendants().forEach(ProcessHandle::destroyForcibly);
				handle.destroyForcibly();
			});
		}
		for (long pid : pids) {
			Optional<ProcessHandle> handle = ProcessHandle.of(pid);
			if (handle.isPresent()) {
				handle.get().onExit().get(30, TimeUnit.SECONDS);
			}
		}
	}

	static Path repositoryRoot() {
		Path current = Path.of(System.getProperty("user.dir")).toAbsolutePath();
		while (current != null && !Files.isRegularFile(current.resolve("AGENTS.md"))) {
			current = current.getParent();
		}
		if (current == null) {
			throw new IllegalStateException("Repository root is unavailable");
		}
		return current;
	}

	static AppD waitForApplication(long seconds) throws Exception {
		Field initing = App.class.getDeclaredField("initing");
		initing.setAccessible(true);
		long end = System.nanoTime() + TimeUnit.SECONDS.toNanos(seconds);
		while (System.nanoTime() < end) {
			AtomicReference<AppD> found = new AtomicReference<>();
			SwingUtilities.invokeAndWait(() -> {
				for (GeoGebraFrame frame : GeoGebraFrame.getInstances()) {
					if (frame.isShowing() && frame.getApplication() != null) {
						found.set(frame.getApplication());
					}
				}
			});
			if (found.get() != null && !(Boolean) initing.get(found.get())) {
				return found.get();
			}
			Thread.sleep(100);
		}
		throw new AssertionError("the application window did not start");
	}

	static <T> T onEdt(Callable<T> task) throws Exception {
		AtomicReference<T> value = new AtomicReference<>();
		AtomicReference<Exception> failure = new AtomicReference<>();
		SwingUtilities.invokeAndWait(() -> {
			try {
				value.set(task.call());
			} catch (Exception exception) {
				failure.set(exception);
			}
		});
		if (failure.get() != null) {
			throw failure.get();
		}
		return value.get();
	}

	static String stacks() {
		StringBuilder text = new StringBuilder();
		for (ThreadInfo info : ManagementFactory.getThreadMXBean().dumpAllThreads(true,
				true)) {
			if ("main".equals(info.getThreadName())
					|| info.getThreadName().startsWith("AWT-EventQueue")) {
				text.append(info);
			}
		}
		return text.toString();
	}

	/**
	 * Observer agent of the Classic JVMs. It acts only when the JVM runs Classic
	 * (the development route or the dispatch marker) and never references a
	 * GeoCeDG product class.
	 */
	public static final class Observer {
		private static volatile Instrumentation instrumentation;

		private Observer() {
		}

		/**
		 * @param arguments unused
		 * @param inst instrumentation of the JVM
		 */
		public static void premain(String arguments, Instrumentation inst) {
			String command = System.getProperty("sun.java.command", "");
			boolean classic = command.startsWith("org.geogebra.desktop.GeoGebra3D")
					|| command.startsWith("org.geocedg.desktop.GeoCeDG " + MARKER);
			if (!classic || System.getenv(PROBE_DIRECTORY) == null) {
				return;
			}
			instrumentation = inst;
			Thread thread = new Thread(Observer::observe, "e1x1-observer");
			thread.setDaemon(true);
			thread.start();
		}

		private static void observe() {
			Path probes = Path.of(System.getenv(PROBE_DIRECTORY));
			long pid = ProcessHandle.current().pid();
			Properties report = new Properties();
			report.setProperty("pid", Long.toString(pid));
			report.setProperty("parentPid", ProcessHandle.current().parent()
					.map(handle -> Long.toString(handle.pid())).orElse(""));
			AppD app = null;
			try {
				app = waitForApplication(REPORT_TIMEOUT_SECONDS);
				Thread.sleep(1000);
				AppD ready = app;
				report.setProperty("app", ready.getClass().getName());
				report.setProperty("config", ready.getConfig().getClass().getName());
				report.setProperty("appCode", ready.getConfig().getAppCode());
				report.setProperty("guiManager", ready.getGuiManager().getClass().getName());
				report.setProperty("frames", onEdt(() -> {
					Set<String> frames = new TreeSet<>();
					for (GeoGebraFrame frame : GeoGebraFrame.getInstances()) {
						frames.add(frame.getClass().getName());
					}
					return String.join(",", frames);
				}));
				report.setProperty("currentFile", onEdt(() -> ready.getCurrentFile() == null
						? "" : ready.getCurrentFile().getAbsolutePath()));
				report.setProperty("constructionSteps", Integer.toString(onEdt(
						() -> ready.getKernel().getConstruction().steps())));
				Field settings = GeoGebraPreferencesD.class.getDeclaredField(
						"PROPERTY_FILEPATH");
				settings.setAccessible(true);
				report.setProperty("settings", String.valueOf(settings.get(null)));
				Set<String> loaded = new TreeSet<>();
				for (Class<?> type : instrumentation.getAllLoadedClasses()) {
					if (GEOCEDG_PRODUCT_CLASSES.contains(type.getName())) {
						loaded.add(type.getName());
					}
				}
				report.setProperty("geocedgProductClassesLoaded", String.join(",", loaded));
				List<String> moduleAccess = new ArrayList<>();
				for (String option : ManagementFactory.getRuntimeMXBean().getInputArguments()) {
					if (option.startsWith("--add-") || option.startsWith("java.")) {
						moduleAccess.add(option);
					}
				}
				report.setProperty("moduleAccess", String.join(" ", moduleAccess));
				report.setProperty("ok", "true");
			} catch (Throwable failure) {
				report.setProperty("ok", "false");
				report.setProperty("failure", String.valueOf(failure));
				report.setProperty("stacks", stacks());
			}
			try {
				store(report, probes.resolve("classic-" + pid + ".properties"));
				String mode = Files.exists(probes.resolve("exit-mode"))
						? Files.readString(probes.resolve("exit-mode")).trim() : "exit";
				if (app != null && "exit".equals(mode)) {
					AppD closing = app;
					SwingUtilities.invokeLater(() -> {
						closing.setSaved();
						closing.exit();
					});
				} else if (app == null) {
					Runtime.getRuntime().halt(3);
				}
			} catch (IOException exception) {
				Runtime.getRuntime().halt(4);
			}
		}
	}

	/** Main class of the real GeoCeDG parent JVM. */
	public static final class ParentDriver {
		private ParentDriver() {
		}

		/**
		 * @param args Templatev7 copy, result file
		 */
		public static void main(String[] args) {
			Path model = Path.of(args[0]);
			Path result = Path.of(args[1]);
			Path probes = Path.of(System.getenv(PROBE_DIRECTORY));
			Properties out = new Properties();
			out.setProperty("parentPid", Long.toString(ProcessHandle.current().pid()));
			try {
				GeoCeDG.main(new String[] {"--showSplash=false", "--language=en"});
				AppD app = waitForApplication(REPORT_TIMEOUT_SECONDS);
				Thread.sleep(1000);
				out.setProperty("parentApp", app.getClass().getName());
				GeoCeDGActionRegistry registry = ((GuiManagerGeoCeDG) app.getGuiManager())
						.getActionRegistry();
				File active = probes.resolveSibling("active-document.cedg").toFile();
				onEdt(() -> {
					app.setCurrentFile(active);
					return null;
				});
				Path mainPreferences = GeoCeDG.getDefaultPreferencesFile();
				out.setProperty("mainPreferences", mainPreferences.toString());
				final String construction = onEdt(() -> constructionXml(app));
				final boolean saved = onEdt(app::isSaved);
				final String preferences = digest(mainPreferences);

				// the Laboratory with the verbose Templatev7; the child closes by itself
				Files.writeString(probes.resolve("exit-mode"), "exit");
				Set<String> known = names(probes);
				onEdt(() -> {
					registry.launchDiagnostic(true, model.toFile());
					return null;
				});
				Properties laboratory = awaitReport(probes, known);
				long laboratoryPid = Long.parseLong(laboratory.getProperty("pid"));
				out.setProperty("laboratoryPid", Long.toString(laboratoryPid));
				Optional<ProcessHandle> handle = ProcessHandle.of(laboratoryPid);
				boolean exited = handle.isEmpty() || handle.get().onExit()
						.completeOnTimeout(null, 90, TimeUnit.SECONDS).get() != null;
				out.setProperty("laboratoryExitedByItself", Boolean.toString(exited));
				out.setProperty("parentResponsiveAfterChildExit", Boolean.toString(
						onEdt(() -> app.getFrame().isShowing())));

				// Open Classic through the real action; the child stays open
				Files.writeString(probes.resolve("exit-mode"), "stay");
				known = names(probes);
				onEdt(() -> {
					registry.invoke("diagnostic.open-classic", new ActionEvent(
							app.getMainComponent(), ActionEvent.ACTION_PERFORMED,
							"diagnostic.open-classic"));
					return null;
				});
				Properties classic = awaitReport(probes, known);
				out.setProperty("classicPid", classic.getProperty("pid"));

				out.setProperty("constructionUnchanged", Boolean.toString(
						construction.equals(onEdt(() -> constructionXml(app)))));
				out.setProperty("savedStateUnchanged", Boolean.toString(
						saved == onEdt(app::isSaved)));
				out.setProperty("currentFileUnchanged", Boolean.toString(
						active.equals(onEdt(app::getCurrentFile))));
				out.setProperty("mainPreferencesUnchanged", Boolean.toString(
						preferences.equals(digest(mainPreferences))));
				out.setProperty("ok", "true");
			} catch (Throwable failure) {
				out.setProperty("ok", "false");
				out.setProperty("failure", String.valueOf(failure));
				out.setProperty("stacks", stacks());
			}
			try {
				store(out, result);
			} catch (IOException ignored) {
				// the test reports the missing result
			}
			// the parent closes without touching its Classic child
			Runtime.getRuntime().halt(0);
		}

		private static Set<String> names(Path probes) throws IOException {
			Set<String> names = new TreeSet<>();
			try (var files = Files.list(probes)) {
				files.forEach(file -> names.add(file.getFileName().toString()));
			}
			return names;
		}

		private static Properties awaitReport(Path probes, Set<String> known)
				throws Exception {
			long end = System.nanoTime() + TimeUnit.SECONDS.toNanos(REPORT_TIMEOUT_SECONDS);
			while (System.nanoTime() < end) {
				try (var files = Files.list(probes)) {
					for (Path file : files.toList()) {
						String name = file.getFileName().toString();
						if (name.startsWith("classic-") && name.endsWith(".properties")
								&& !known.contains(name)) {
							return load(file);
						}
					}
				}
				Thread.sleep(250);
			}
			throw new AssertionError("no Classic child reported within "
					+ REPORT_TIMEOUT_SECONDS + " s");
		}

		private static String constructionXml(AppD app) {
			XMLStringBuilder xml = new XMLStringBuilder();
			app.getKernel().getConstruction().getConstructionXML(xml, true);
			return xml.toString();
		}

		private static String digest(Path file) throws Exception {
			if (!Files.isRegularFile(file)) {
				return "ABSENT";
			}
			return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
					.digest(Files.readAllBytes(file)));
		}
	}
}
