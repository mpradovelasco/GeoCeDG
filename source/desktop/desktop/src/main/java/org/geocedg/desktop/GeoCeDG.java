/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import org.geocedg.desktop.resources.GeoCeDGBrandingResource;
import org.geogebra.desktop.CommandLineArguments;
import org.geogebra.desktop.GeoGebra;
import org.geogebra.desktop.GeoGebra3D;
import org.geogebra.desktop.main.GeoGebraPreferencesD;

/**
 * Explicit Desktop entry point for GeoCeDG.
 */
public final class GeoCeDG {

	private static final String SETTINGS_FILE = "preferences.properties";

	private GeoCeDG() {
	}

	/**
	 * Run GeoCeDG.
	 *
	 * @param args command line arguments
	 */
	public static void main(String[] args) {
		String[] effectiveArguments = args == null ? new String[0] : args;
		if (GeoCeDGClassicDiagnosticLaunch.isDispatch(effectiveArguments)) {
			// PRE-G9B-R6-plus-E1-X1: internal launcher protocol of the separate
			// diagnostic Classic process; it runs before any GeoCeDG preference,
			// profile or frame exists and never continues into GeoCeDG start-up.
			startClassicDiagnostic(effectiveArguments);
			return;
		}
		CommandLineArguments parsedArguments = new CommandLineArguments(
				effectiveArguments);
		if (!parsedArguments.containsArg("settingsfile")) {
			Path preferencesFile = getDefaultPreferencesFile();
			try {
				Files.createDirectories(preferencesFile.getParent());
			} catch (IOException exception) {
				throw new IllegalStateException(
						"Cannot create GeoCeDG preferences directory: "
								+ preferencesFile.getParent(), exception);
			}
			GeoGebraPreferencesD.setPropertyFileName(preferencesFile.toString());
		}
		GeoGebra.doMain(effectiveArguments, GeoCeDGFrame::new,
				GeoCeDG::getSplashResource);
	}

	private static void startClassicDiagnostic(String[] args) {
		Path preferences = GeoCeDGClassicDiagnosticLaunch.preferences(
				getDefaultPreferencesFile().getParent(), false);
		try {
			Files.createDirectories(preferences.getParent());
		} catch (IOException exception) {
			throw new IllegalStateException(
					"Cannot create GeoCeDG preferences directory: "
							+ preferences.getParent(), exception);
		}
		GeoGebra3D.main(GeoCeDGClassicDiagnosticLaunch.classicDispatchArguments(
				args, preferences));
	}

	/**
	 * @return isolated default preference file for the current user
	 */
	public static Path getDefaultPreferencesFile() {
		String applicationData = System.getenv("APPDATA");
		Path base = applicationData == null || applicationData.isBlank()
				? Path.of(System.getProperty("user.home"), "GeoCeDG")
				: Path.of(applicationData, "GeoCeDG");
		return base.resolve("5.4").resolve(SETTINGS_FILE).toAbsolutePath();
	}

	static URL getSplashResource() {
		return GeoCeDGBrandingResource.STARTUP_SPLASH.getRequiredUrl();
	}
}
