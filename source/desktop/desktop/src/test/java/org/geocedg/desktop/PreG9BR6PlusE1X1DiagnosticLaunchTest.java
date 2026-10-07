/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * PRE-G9B-R6-plus-E1-X1 focused tests of the diagnostic Classic launch: the
 * first-position dispatch marker, the packaged and development commands, the
 * module-access filter, the discarded child streams, the isolated preference
 * files and the Laboratory resource rule. Pure functions only: no application,
 * no process, no real {@code jpackage.app-path}.
 */
class PreG9BR6PlusE1X1DiagnosticLaunchTest {

	private static final String MARKER = "--classic-diagnostic";
	private static final List<String> MODULE_OPTIONS = List.of(
			"--add-exports=java.base/java.lang=ALL-UNNAMED",
			"--add-exports=java.desktop/sun.awt=ALL-UNNAMED",
			"--add-exports=java.desktop/sun.java2d=ALL-UNNAMED",
			"--enable-native-access=ALL-UNNAMED");

	@TempDir
	Path temporary;

	@Test
	void markerIsRecognizedOnlyAsTheExactFirstArgument() {
		assertEquals(MARKER, GeoCeDGClassicDiagnosticLaunch.DISPATCH_ARGUMENT);
		assertTrue(GeoCeDGClassicDiagnosticLaunch.isDispatch(new String[] {MARKER}));
		assertTrue(GeoCeDGClassicDiagnosticLaunch.isDispatch(
				new String[] {MARKER, "--settingsfile=x.properties", "model.ggb"}));
		for (String[] normal : List.of(new String[0], new String[] {"model.cedg"},
				new String[] {"--settingsfile=x.properties", MARKER},
				new String[] {"C:\\work\\--classic-diagnostic.cedg"},
				new String[] {"--classic-diagnostic.ggb"},
				new String[] {"--classic-diagnostic=true"},
				new String[] {"--CLASSIC-DIAGNOSTIC"},
				new String[] {" --classic-diagnostic"},
				new String[] {"-classic-diagnostic"})) {
			assertFalse(GeoCeDGClassicDiagnosticLaunch.isDispatch(normal),
					String.join(" ", normal));
		}
		assertFalse(GeoCeDGClassicDiagnosticLaunch.isDispatch(null));
	}

	@Test
	void dispatchRemovesOnlyTheMarkerAndKeepsEveryOtherArgument() {
		Path defaults = temporary.resolve("classic-diagnostic.properties");
		String[] route = {MARKER, "--showSplash=false",
				"--settingsfile=C:\\p\\laboratory.properties", "C:\\m\\model.ggb"};
		assertArrayEquals(new String[] {"--showSplash=false",
				"--settingsfile=C:\\p\\laboratory.properties", "C:\\m\\model.ggb"},
				GeoCeDGClassicDiagnosticLaunch.classicDispatchArguments(route, defaults));
		// caller values are never replaced, whatever their case or value
		assertArrayEquals(new String[] {"--showsplash=true", "--settingsFile=a.properties"},
				GeoCeDGClassicDiagnosticLaunch.classicDispatchArguments(new String[] {
						MARKER, "--showsplash=true", "--settingsFile=a.properties"}, defaults));
		// a bare marker gets no upstream splash and isolated Classic preferences
		assertArrayEquals(new String[] {"--showSplash=false", "--settingsfile=" + defaults},
				GeoCeDGClassicDiagnosticLaunch.classicDispatchArguments(
						new String[] {MARKER}, defaults));
		assertArrayEquals(new String[] {"--showSplash=false", "--settingsfile=" + defaults,
				"--language=en", "C:\\m\\tool.ggt"},
				GeoCeDGClassicDiagnosticLaunch.classicDispatchArguments(new String[] {
						MARKER, "--language=en", "C:\\m\\tool.ggt"}, defaults));
		for (String argument : GeoCeDGClassicDiagnosticLaunch.classicDispatchArguments(
				new String[] {MARKER, MARKER + ".ggb"}, defaults)) {
			assertNotEquals(MARKER, argument);
		}
	}

	@Test
	void normalInvocationIsNotADispatch() {
		assertThrows(IllegalArgumentException.class,
				() -> GeoCeDGClassicDiagnosticLaunch.classicDispatchArguments(
						new String[] {"--settingsfile=x.properties"}, temporary));
		// the shapes of an ordinary, an association and a development start
		for (String[] normal : List.of(new String[] {"--language=en"},
				new String[] {"C:\\Users\\a\\Documents\\drawing.cedg"},
				new String[] {"--settingsfile=C:\\p\\preferences.properties",
						"--language=en"})) {
			assertFalse(GeoCeDGClassicDiagnosticLaunch.isDispatch(normal));
		}
	}

	@Test
	void packagedCommandStartsTheRunningLauncherWithTheMarker() throws Exception {
		Path launcher = Files.createFile(temporary.resolve("GeoCeDG.exe"));
		Path preferences = temporary.resolve("classic-diagnostic.properties");
		List<String> command = GeoCeDGClassicDiagnosticLaunch.command(
				packaged(launcher.toString()), preferences, null);
		assertEquals(List.of(launcher.toString(), MARKER, "--showSplash=false",
				"--settingsfile=" + preferences), command);
	}

	@Test
	void invalidPackagedLauncherFailsClosed() throws Exception {
		Path preferences = temporary.resolve("classic-diagnostic.properties");
		Path directory = Files.createDirectory(temporary.resolve("GeoCeDG-dir.exe"));
		for (String applicationPath : List.of(
				temporary.resolve("missing").resolve("GeoCeDG.exe").toString(), "", "   ",
				directory.toString(), "C:\\bad\u0000path\\GeoCeDG.exe")) {
			IOException failure = assertThrows(IOException.class,
					() -> GeoCeDGClassicDiagnosticLaunch.command(packaged(applicationPath),
							preferences, null), applicationPath);
			assertTrue(failure.getMessage().startsWith("Packaged GeoCeDG launcher is missing"),
					failure.getMessage());
		}
	}

	@Test
	void packagedModeNeverFallsBackToTheJavaRuntime() throws Exception {
		Path home = javaHome("javaw.exe", "java.exe");
		Path preferences = temporary.resolve("classic-diagnostic.properties");
		GeoCeDGClassicDiagnosticLaunch.Environment missing =
				new GeoCeDGClassicDiagnosticLaunch.Environment(
						temporary.resolve("absent.exe").toString(), home, "cp", MODULE_OPTIONS);
		assertThrows(IOException.class,
				() -> GeoCeDGClassicDiagnosticLaunch.command(missing, preferences, null));
		Path launcher = Files.createFile(temporary.resolve("GeoCeDG.exe"));
		List<String> command = GeoCeDGClassicDiagnosticLaunch.command(
				new GeoCeDGClassicDiagnosticLaunch.Environment(launcher.toString(), home, "cp",
						MODULE_OPTIONS), preferences, null);
		for (String argument : command) {
			assertFalse(argument.startsWith(home.toString()), argument);
			assertFalse(argument.startsWith("--add-") || argument.startsWith("--enable-"),
					argument);
		}
		assertFalse(command.contains("-cp"));
		assertFalse(command.contains(GeoCeDGClassicDiagnosticLaunch.CLASSIC_MAIN_CLASS));
	}

	@Test
	void developmentCommandStartsTheClassicMainClassWithItsJava() throws Exception {
		Path home = javaHome("javaw.exe", "java.exe", "java");
		Path preferences = temporary.resolve("classic-diagnostic.properties");
		List<String> command = GeoCeDGClassicDiagnosticLaunch.command(
				development(home, MODULE_OPTIONS), preferences, null);
		List<String> expected = new ArrayList<>();
		expected.add(home.resolve("bin").resolve("javaw.exe").toString());
		expected.addAll(MODULE_OPTIONS);
		expected.addAll(List.of("-cp", "class-path", "org.geogebra.desktop.GeoGebra3D",
				"--showSplash=false", "--settingsfile=" + preferences));
		assertEquals(expected, command);
		assertFalse(command.contains(MARKER));
		assertFalse(command.contains("org.geocedg.desktop.GeoCeDG"));
		// the established fallback order
		assertEquals(javaHome("java.exe", "java").resolve("bin").resolve("java.exe")
				.toString(), GeoCeDGClassicDiagnosticLaunch.command(
						development(temporary.resolve("jdk-java.exe-java"), List.of()),
						preferences, null).get(0));
		assertEquals(javaHome("java").resolve("bin").resolve("java").toString(),
				GeoCeDGClassicDiagnosticLaunch.command(
						development(temporary.resolve("jdk-java"), List.of()),
						preferences, null).get(0));
		Path empty = Files.createDirectories(temporary.resolve("empty").resolve("bin"))
				.getParent();
		assertThrows(IOException.class, () -> GeoCeDGClassicDiagnosticLaunch.command(
				development(empty, List.of()), preferences, null));
	}

	@Test
	void developmentFilterKeepsTheModuleAccessFamilies() {
		List<String> inputs = List.of("--add-exports=java.base/java.lang=ALL-UNNAMED",
				"--add-opens=java.desktop/java.awt=ALL-UNNAMED",
				"--enable-native-access=ALL-UNNAMED",
				"--add-exports", "java.desktop/sun.awt=ALL-UNNAMED",
				"--add-opens", "java.base/java.util=ALL-UNNAMED",
				"--enable-native-access", "ALL-UNNAMED");
		assertEquals(inputs, GeoCeDGClassicDiagnosticLaunch.moduleAccessOptions(inputs));
		assertEquals(MODULE_OPTIONS,
				GeoCeDGClassicDiagnosticLaunch.moduleAccessOptions(MODULE_OPTIONS));
	}

	@Test
	void developmentFilterDropsUnrelatedVmArguments() {
		List<String> unrelated = List.of("-javaagent:C:\\tools\\agent.jar=x",
				"-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005",
				"-Xmx512m", "-Xms64m", "-Xss4m", "-XX:+HeapDumpOnOutOfMemoryError",
				"-XX:MaxMetaspaceSize=256m", "-Dfile.encoding=UTF-8",
				"-Dorg.gradle.test.worker=7", "-Djava.security.manager=allow", "-ea",
				"-Duser.language=en", "--add-modules=ALL-SYSTEM", "--patch-module=x=y",
				"--add-exportsX=java.base/java.lang=ALL-UNNAMED", "--add-reads=a=b",
				"--illegal-access=permit", "-Djpackage.app-path=C:\\x\\GeoCeDG.exe");
		assertEquals(List.of(), GeoCeDGClassicDiagnosticLaunch.moduleAccessOptions(unrelated));
		List<String> mixed = new ArrayList<>(unrelated);
		mixed.add(3, "--enable-native-access=ALL-UNNAMED");
		mixed.add("--add-exports=java.desktop/sun.java2d=ALL-UNNAMED");
		assertEquals(List.of("--enable-native-access=ALL-UNNAMED",
				"--add-exports=java.desktop/sun.java2d=ALL-UNNAMED"),
				GeoCeDGClassicDiagnosticLaunch.moduleAccessOptions(mixed));
		// a family name as the last token has no value and is not forwarded
		assertEquals(List.of(), GeoCeDGClassicDiagnosticLaunch.moduleAccessOptions(
				List.of("--add-exports")));
	}

	@Test
	void childStandardOutputIsDiscarded() {
		ProcessBuilder builder = GeoCeDGClassicDiagnosticLaunch.processBuilder(
				List.of("GeoCeDG.exe", MARKER));
		assertEquals(ProcessBuilder.Redirect.DISCARD, builder.redirectOutput());
		assertEquals(List.of("GeoCeDG.exe", MARKER), builder.command());
	}

	@Test
	void childStandardErrorIsDiscarded() {
		ProcessBuilder builder = GeoCeDGClassicDiagnosticLaunch.processBuilder(
				List.of("GeoCeDG.exe", MARKER));
		assertEquals(ProcessBuilder.Redirect.DISCARD, builder.redirectError());
		assertFalse(builder.redirectErrorStream(),
				"stderr is discarded on its own, never merged into a pipe");
	}

	@Test
	void classicDiagnosticPreferencesAreIsolated() throws Exception {
		Path main = GeoCeDG.getDefaultPreferencesFile();
		Path classic = GeoCeDGClassicDiagnosticLaunch.preferences(main.getParent(), false);
		assertEquals(main.getParent(), classic.getParent());
		assertEquals("classic-diagnostic.properties", classic.getFileName().toString());
		assertNotEquals(main, classic);
		List<String> command = GeoCeDGClassicDiagnosticLaunch.command(
				development(javaHome("javaw.exe"), List.of()), classic, null);
		assertTrue(command.contains("--settingsfile=" + classic));
		assertFalse(String.join(" ", command).contains(main.getFileName().toString()));
	}

	@Test
	void laboratoryPreferencesAreIsolated() throws Exception {
		Path main = GeoCeDG.getDefaultPreferencesFile();
		Path laboratory = GeoCeDGClassicDiagnosticLaunch.preferences(main.getParent(), true);
		Path classic = GeoCeDGClassicDiagnosticLaunch.preferences(main.getParent(), false);
		assertEquals("preferences.properties", main.getFileName().toString());
		assertEquals("laboratory.properties", laboratory.getFileName().toString());
		assertEquals(3, java.util.Set.of(main, laboratory, classic).size(),
				"three distinct preference stores in one directory");
		Path launcher = Files.createFile(temporary.resolve("GeoCeDG.exe"));
		List<String> command = GeoCeDGClassicDiagnosticLaunch.command(
				packaged(launcher.toString()), laboratory, ggb("model.ggb"));
		assertTrue(command.contains("--settingsfile=" + laboratory));
		assertFalse(command.contains("--settingsfile=" + classic));
	}

	@Test
	void validGgbIsForwardedLastInBothModes() throws Exception {
		File model = ggb("Templatev7.ggb");
		assertForwardedLast(model);
	}

	@Test
	void validGgtIsForwardedLastInBothModes() throws Exception {
		File tool = ggb("CirclebyD.ggt");
		assertForwardedLast(tool);
		assertForwardedLast(ggb("UPPER.GGB"));
	}

	@Test
	void invalidOrMissingResourceIsRejectedBeforeLaunch() throws Exception {
		Path launcher = Files.createFile(temporary.resolve("GeoCeDG.exe"));
		Path preferences = temporary.resolve("laboratory.properties");
		File text = Files.createFile(temporary.resolve("not-a-model.txt")).toFile();
		File missing = temporary.resolve("missing-model.ggb").toFile();
		File directory = Files.createDirectory(temporary.resolve("folder.ggb")).toFile();
		File noExtension = Files.createFile(temporary.resolve("model")).toFile();
		for (File resource : List.of(text, missing, directory, noExtension)) {
			for (GeoCeDGClassicDiagnosticLaunch.Environment environment : List.of(
					packaged(launcher.toString()), development(javaHome("javaw.exe"),
							List.of()))) {
				IOException failure = assertThrows(IOException.class,
						() -> GeoCeDGClassicDiagnosticLaunch.command(environment,
								preferences, resource), resource.toString());
				assertEquals("Diagnostic resource must be an existing GGB/GGT file",
						failure.getMessage());
			}
		}
	}

	@Test
	void activeDocumentIsNeverForwarded() throws Exception {
		// the command has no document input: every route yields exactly its fixed
		// arguments plus at most the explicit Laboratory file
		Path launcher = Files.createFile(temporary.resolve("GeoCeDG.exe"));
		Path preferences = temporary.resolve("classic-diagnostic.properties");
		Path activeDocument = Files.createFile(temporary.resolve("active.cedg"));
		List<String> classic = GeoCeDGClassicDiagnosticLaunch.command(
				packaged(launcher.toString()), preferences, null);
		assertEquals(4, classic.size());
		List<String> development = GeoCeDGClassicDiagnosticLaunch.command(
				development(javaHome("javaw.exe"), MODULE_OPTIONS), preferences, null);
		assertEquals(MODULE_OPTIONS.size() + 6, development.size());
		for (List<String> command : List.of(classic, development)) {
			assertFalse(String.join(" ", command).contains(activeDocument.getFileName()
					.toString()));
			assertFalse(String.join(" ", command).contains(".cedg"));
		}
	}

	private void assertForwardedLast(File resource) throws Exception {
		Path launcher = temporary.resolve("GeoCeDG.exe");
		if (!Files.exists(launcher)) {
			Files.createFile(launcher);
		}
		Path preferences = temporary.resolve("laboratory.properties");
		for (GeoCeDGClassicDiagnosticLaunch.Environment environment : List.of(
				packaged(launcher.toString()), development(javaHome("javaw.exe"),
						MODULE_OPTIONS))) {
			List<String> command = GeoCeDGClassicDiagnosticLaunch.command(environment,
					preferences, resource);
			assertEquals(resource.getAbsolutePath(), command.get(command.size() - 1));
			assertEquals("--settingsfile=" + preferences, command.get(command.size() - 2));
		}
	}

	private File ggb(String name) throws IOException {
		Path file = temporary.resolve("models").resolve(name);
		Files.createDirectories(file.getParent());
		if (!Files.exists(file)) {
			Files.createFile(file);
		}
		return file.toFile();
	}

	private Path javaHome(String... launchers) throws IOException {
		Path home = temporary.resolve("jdk-" + String.join("-", launchers));
		Path bin = Files.createDirectories(home.resolve("bin"));
		for (String launcher : launchers) {
			if (!Files.exists(bin.resolve(launcher))) {
				Files.createFile(bin.resolve(launcher));
			}
		}
		return home;
	}

	private GeoCeDGClassicDiagnosticLaunch.Environment packaged(String applicationPath)
			throws IOException {
		return new GeoCeDGClassicDiagnosticLaunch.Environment(applicationPath,
				javaHome("javaw.exe"), "class-path", MODULE_OPTIONS);
	}

	private static GeoCeDGClassicDiagnosticLaunch.Environment development(Path javaHome,
			List<String> inputArguments) {
		return new GeoCeDGClassicDiagnosticLaunch.Environment(null, javaHome, "class-path",
				inputArguments);
	}
}
