/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.geogebra.common.util.StringUtil;
import org.geogebra.desktop.CommandLineArguments;
import org.geogebra.desktop.GeoGebra;
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
		String[] safeArguments = withoutUpstreamSplash(args);
		CommandLineArguments parsedArguments = new CommandLineArguments(safeArguments);
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
		GeoGebra.doMain(safeArguments, GeoCeDGFrame::new);
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

	static String[] withoutUpstreamSplash(String[] args) {
		List<String> safeArguments = new ArrayList<>();
		if (args != null) {
			for (String argument : args) {
				if (!StringUtil.toLowerCaseUS(argument).startsWith("--showsplash")) {
					safeArguments.add(argument);
				}
			}
		}
		safeArguments.add("--showSplash=false");
		return safeArguments.toArray(new String[0]);
	}
}
