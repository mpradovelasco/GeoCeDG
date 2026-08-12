/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop.locus;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.geogebra.common.util.StringUtil;
import org.geogebra.desktop.GeoGebra;
import org.geogebra.desktop.main.GeoGebraPreferencesD;

/** Explicit developer-only entry point for visual Locus V2 inspection. */
public final class LocusV2Laboratory {
	private static final String SETTINGS_FILE = "laboratory.properties";

	private LocusV2Laboratory() {
	}

	/**
	 * Starts an isolated GeoCeDG process. No normal application preference or
	 * construction file is reused.
	 *
	 * @param args ordinary Desktop command-line arguments
	 */
	public static void main(String[] args) {
		Path settings = createTemporarySettingsFile();
		GeoGebraPreferencesD.setPropertyFileName(settings.toString());
		String[] laboratoryArguments = new String[(args == null ? 0 : args.length) + 1];
		if (args != null) {
			System.arraycopy(args, 0, laboratoryArguments, 0, args.length);
		}
		laboratoryArguments[laboratoryArguments.length - 1] =
				"--settingsFile=" + settings;
		GeoGebra.doMain(withoutUpstreamSplash(laboratoryArguments),
				LocusV2LaboratoryFrame::new);
	}

	private static String[] withoutUpstreamSplash(String[] args) {
		List<String> safeArguments = new ArrayList<>();
		for (String argument : args) {
			if (!StringUtil.toLowerCaseUS(argument).startsWith("--showsplash")) {
				safeArguments.add(argument);
			}
		}
		safeArguments.add("--showSplash=false");
		return safeArguments.toArray(new String[0]);
	}

	static Path createTemporarySettingsFile() {
		try {
			Path directory = Files.createTempDirectory("geocedg-locus-v2-laboratory-");
			directory.toFile().deleteOnExit();
			Path settings = directory.resolve(SETTINGS_FILE).toAbsolutePath();
			settings.toFile().deleteOnExit();
			return settings;
		} catch (IOException exception) {
			throw new IllegalStateException(
					"Cannot create isolated Locus V2 laboratory settings", exception);
		}
	}
}
