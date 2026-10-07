/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import org.geogebra.desktop.CommandLineArguments;

/**
 * Separate-process launch of the diagnostic Classic session
 * (PRE-G9B-R6-plus-E1-X1).
 *
 * <p>A jpackage launch, recognized by {@code jpackage.app-path}, starts that same
 * launcher with the private first-position {@link #DISPATCH_ARGUMENT}, which
 * {@link GeoCeDG#main} hands to the unchanged Classic entry point before any
 * GeoCeDG state exists. Without that property the development route starts the
 * Java of the running JVM on the Classic main class. The child never receives the
 * active document, its standard streams are discarded, and it uses its own
 * isolated preference file. The packaged route never falls back to a Java
 * runtime: the jpackage runtime has none.
 */
final class GeoCeDGClassicDiagnosticLaunch {

	/** private launcher protocol; honored only as the first argument */
	static final String DISPATCH_ARGUMENT = "--classic-diagnostic";
	/** preference file of the Open Classic diagnostic session */
	static final String CLASSIC_PREFERENCES = "classic-diagnostic.properties";
	/** preference file of the Legacy laboratory */
	static final String LABORATORY_PREFERENCES = "laboratory.properties";
	static final String CLASSIC_MAIN_CLASS = "org.geogebra.desktop.GeoGebra3D";
	private static final String SPLASH_OFF = "--showSplash=false";
	private static final String SETTINGS = "--settingsfile=";
	/** the only JVM option families the development child inherits */
	private static final List<String> MODULE_ACCESS_OPTIONS = List.of(
			"--add-exports", "--add-opens", "--enable-native-access");
	/** development launchers in order of preference */
	private static final List<String> JAVA_LAUNCHERS = List.of(
			"javaw.exe", "java.exe", "java");

	/** Facts of the running JVM that select and build the child command. */
	static final class Environment {
		private final String applicationPath;
		private final Path javaHome;
		private final String classPath;
		private final List<String> inputArguments;

		/**
		 * @param applicationPath value of {@code jpackage.app-path}, or null outside a
		 *        jpackage launch
		 * @param javaHome runtime home of the running JVM
		 * @param classPath class path of the running JVM
		 * @param inputArguments JVM input arguments of the running JVM
		 */
		Environment(String applicationPath, Path javaHome, String classPath,
				List<String> inputArguments) {
			this.applicationPath = applicationPath;
			this.javaHome = javaHome;
			this.classPath = classPath;
			this.inputArguments = List.copyOf(inputArguments);
		}

		/** @return the environment of this JVM */
		static Environment current() {
			return new Environment(
					System.getProperty(GeoCeDGBundledToolCatalog.LAUNCHER_PROPERTY),
					Path.of(System.getProperty("java.home")),
					System.getProperty("java.class.path"),
					ManagementFactory.getRuntimeMXBean().getInputArguments());
		}

		/** @return true for a jpackage launch, even when its path is unusable */
		boolean isPackaged() {
			return applicationPath != null;
		}
	}

	private GeoCeDGClassicDiagnosticLaunch() {
		// utility class
	}

	/**
	 * @param preferencesDirectory directory of the GeoCeDG preference file
	 * @param laboratory true for the Legacy laboratory, false for Open Classic
	 * @return the isolated preference file of that route
	 */
	static Path preferences(Path preferencesDirectory, boolean laboratory) {
		return preferencesDirectory.resolve(
				laboratory ? LABORATORY_PREFERENCES : CLASSIC_PREFERENCES);
	}

	/**
	 * @param environment facts of the running JVM
	 * @param preferences isolated preference file of the route
	 * @param resource explicit user-selected GGB/GGT file, or null
	 * @return the child command; it never contains the active document
	 * @throws IOException when the resource is not admissible or the launcher is
	 *         unusable; nothing has been started
	 */
	static List<String> command(Environment environment, Path preferences,
			File resource) throws IOException {
		List<String> classicArguments = classicArguments(preferences, resource);
		List<String> command = new ArrayList<>();
		if (environment.isPackaged()) {
			command.add(packagedLauncher(environment.applicationPath).toString());
			command.add(DISPATCH_ARGUMENT);
		} else {
			command.add(developmentJava(environment.javaHome).toString());
			command.addAll(moduleAccessOptions(environment.inputArguments));
			command.add("-cp");
			command.add(Objects.requireNonNull(environment.classPath,
					"java.class.path"));
			command.add(CLASSIC_MAIN_CLASS);
		}
		command.addAll(classicArguments);
		return List.copyOf(command);
	}

	/**
	 * @param command child command
	 * @return a builder whose child output and error streams are discarded, so an
	 *         unread pipe can never block the child
	 */
	static ProcessBuilder processBuilder(List<String> command) {
		return new ProcessBuilder(command)
				.redirectOutput(ProcessBuilder.Redirect.DISCARD)
				.redirectError(ProcessBuilder.Redirect.DISCARD);
	}

	/**
	 * @param inputArguments JVM input arguments of the running JVM
	 * @return only the module-access options, in order; a family name given as a
	 *         separate token keeps its value token
	 */
	static List<String> moduleAccessOptions(List<String> inputArguments) {
		List<String> options = new ArrayList<>();
		for (int i = 0; i < inputArguments.size(); i++) {
			String argument = inputArguments.get(i);
			if (MODULE_ACCESS_OPTIONS.stream()
					.anyMatch(family -> argument.startsWith(family + "="))) {
				options.add(argument);
			} else if (MODULE_ACCESS_OPTIONS.contains(argument)
					&& i + 1 < inputArguments.size()) {
				options.add(argument);
				options.add(inputArguments.get(++i));
			}
		}
		return List.copyOf(options);
	}

	/**
	 * @param args entry-point arguments
	 * @return true only when the first argument is exactly the dispatch marker
	 */
	static boolean isDispatch(String[] args) {
		return args != null && args.length > 0 && DISPATCH_ARGUMENT.equals(args[0]);
	}

	/**
	 * Arguments for the Classic entry point of a dispatch: only the marker is
	 * removed and every other argument is kept unchanged; the splash is turned off
	 * and the isolated Classic preferences are selected only when the caller gave
	 * no such argument.
	 *
	 * @param args entry-point arguments starting with the marker
	 * @param defaultPreferences isolated Classic preference file
	 * @return arguments for {@code GeoGebra3D.main}
	 */
	static String[] classicDispatchArguments(String[] args, Path defaultPreferences) {
		if (!isDispatch(args)) {
			throw new IllegalArgumentException("Not a Classic diagnostic dispatch");
		}
		List<String> remaining = Arrays.asList(args).subList(1, args.length);
		CommandLineArguments parsed = new CommandLineArguments(
				remaining.toArray(new String[0]));
		List<String> classic = new ArrayList<>();
		if (!parsed.containsArg("showSplash")) {
			classic.add(SPLASH_OFF);
		}
		if (!parsed.containsArg("settingsfile")) {
			classic.add(SETTINGS + defaultPreferences);
		}
		classic.addAll(remaining);
		return classic.toArray(new String[0]);
	}

	private static List<String> classicArguments(Path preferences, File resource)
			throws IOException {
		List<String> arguments = new ArrayList<>(List.of(SPLASH_OFF,
				SETTINGS + preferences));
		if (resource != null) {
			String name = resource.getName().toLowerCase(Locale.ROOT);
			if (!resource.isFile() || !(name.endsWith(".ggb") || name.endsWith(".ggt"))) {
				throw new IOException("Diagnostic resource must be an existing GGB/GGT file");
			}
			arguments.add(resource.getAbsolutePath());
		}
		return arguments;
	}

	private static Path packagedLauncher(String applicationPath) throws IOException {
		Path launcher;
		try {
			launcher = applicationPath.isBlank() ? null
					: Path.of(applicationPath).toAbsolutePath().normalize();
		} catch (InvalidPathException exception) {
			launcher = null;
		}
		if (launcher == null || !Files.isRegularFile(launcher)) {
			throw new IOException("Packaged GeoCeDG launcher is missing: "
					+ applicationPath);
		}
		return launcher;
	}

	private static Path developmentJava(Path javaHome) throws IOException {
		Path bin = javaHome.resolve("bin");
		for (String name : JAVA_LAUNCHERS) {
			Path candidate = bin.resolve(name);
			if (Files.isRegularFile(candidate)) {
				return candidate;
			}
		}
		throw new IOException("No Java launcher in " + bin);
	}
}
